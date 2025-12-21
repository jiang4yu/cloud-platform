package com.maxinhai.platform.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maxinhai.platform.annotation.MqSubscribe;
import com.maxinhai.platform.bo.MqBeanMethodBO;
import com.maxinhai.platform.bo.MqClientBO;
import com.maxinhai.platform.enums.ConnectType;
import com.maxinhai.platform.handler.callback.GlobalMqCallback;
import com.maxinhai.platform.mapper.ConnectConfigMapper;
import com.maxinhai.platform.po.ConnectConfig;
import com.maxinhai.platform.utils.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName：ApiHandler
 * @Author: XinHai.Ma
 * @Date: 2025/8/21 11:39
 * @Description: MQ消息处理器
 */
@Slf4j
@Component
public class MqHandler implements CommandLineRunner {

    @Resource
    private ConnectConfigMapper connectMapper;
    @Resource
    private ApplicationContext applicationContext;

    // 缓存：clientId -> RabbitTemplate（生产者客户端）
    private final Map<String, RabbitTemplate> rabbitTemplateMap = new ConcurrentHashMap<>();
    // 缓存：clientId+topic -> 监听器容器（避免重复注册）
    private final Map<String, SimpleMessageListenerContainer> listenerContainerMap = new ConcurrentHashMap<>();
    // 缓存：clientId+topic -> 注解方法集合（存储@RabbitMqSubscribe标注的方法）
    private final Map<String, Set<MqBeanMethodBO>> rabbitMqHandlerMap = new ConcurrentHashMap<>();


    @Override
    public void run(String... args) throws Exception {
        log.info("开始扫描MQ订阅注解");
        scanMqSubscribeAnnotations(); // 新增：扫描注解
        log.info("MQ订阅注解扫描完成，共扫描到{}个监听方法", rabbitMqHandlerMap.size());

        log.info("开始初始化MQ客户端");
        initClients();
        log.info("MQ客户端初始化完成");

        // 监听消息队列心跳主题
        subscribe("mq", "/heartbeat", msg -> {
            log.info("接收MQ消息:{}", new String(msg.getBody()));
        });
    }

    /**
     * 新增：扫描所有带有@MqSubscribe的方法
     */
    private void scanMqSubscribeAnnotations() throws BeansException {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                MqSubscribe annotation = method.getAnnotation(MqSubscribe.class);
                if (annotation != null) {
                    // 注册注解方法到分发器
                    registerHandler(
                            annotation.clientId(),
                            annotation.routingKey(),
                            annotation.exchange(),
                            annotation.queue(),
                            annotation.routingKey(),
                            new MqBeanMethodBO(bean, method, annotation)
                    );
                }
            }
        }
    }

    /**
     * 初始化全部客户端
     */
    private void initClients() {
        List<ConnectConfig> connectList = connectMapper.selectList(new LambdaQueryWrapper<ConnectConfig>()
                .select(ConnectConfig::getId, ConnectConfig::getKey, ConnectConfig::getType,
                        ConnectConfig::getIp, ConnectConfig::getPort,
                        ConnectConfig::getUsername, ConnectConfig::getPassword)
                .eq(ConnectConfig::getType, ConnectType.MQ));
        if (CollectionUtils.isEmpty(connectList)) {
            return;
        }

        for (ConnectConfig connectConfig : connectList) {
            RabbitTemplate rabbitTemplate = initClient(MqClientBO.build(connectConfig));
            rabbitTemplateMap.putIfAbsent(connectConfig.getKey(), rabbitTemplate);
            SimpleMessageListenerContainer listenerContainer = createAllTopicListenerContainer(connectConfig.getKey());
            listenerContainer.start();
            listenerContainerMap.putIfAbsent(connectConfig.getKey(), listenerContainer);
        }
    }

    /**
     * 初始化RabbitMQ客户端
     *
     * @param mqClient MQ客户端配置
     * @return RabbitTemplate
     */
    private RabbitTemplate initClient(MqClientBO mqClient) {
        // 1. 创建连接工厂
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(mqClient.getIp());
        connectionFactory.setPort(mqClient.getPort());
        connectionFactory.setUsername(mqClient.getUsername());
        connectionFactory.setPassword(mqClient.getPassword());
        // 连接池配置：最大空闲连接数（默认2）
        connectionFactory.setConnectionCacheSize(5);
        // 连接超时时间（默认60秒）
        connectionFactory.setConnectionTimeout(30000);
        // 设置虚拟主机（默认"/"）
        connectionFactory.setVirtualHost("/");

        // 2. 创建RabbitTemplate并缓存
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * 创建监听所有topic的容器（Topic交换机+通配符）
     */
    private SimpleMessageListenerContainer createAllTopicListenerContainer(String clientId) {
        RabbitTemplate template = rabbitTemplateMap.get(clientId);
        ConnectionFactory factory = template.getConnectionFactory();

        // 1. 声明Topic交换机（若不存在）
        TopicExchange allTopicExchange = new TopicExchange(RabbitMQUtils.DEFAULT_EXCHANGE, true, false);
        RabbitAdmin admin = new RabbitAdmin(factory);
        admin.declareExchange(allTopicExchange);

        // 2. 声明队列（接收所有topic的消息）
        Queue allTopicQueue = QueueBuilder.durable(RabbitMQUtils.DEFAULT_QUEUE).build();
        admin.declareQueue(allTopicQueue);

        // 3. 绑定队列到交换机，使用通配符路由键#（匹配所有路由键）
        Binding binding = BindingBuilder.bind(allTopicQueue)
                .to(allTopicExchange)
                .with(RabbitMQUtils.DEFAULT_ROUTING_KEY); // 通配符：匹配所有路由键
        admin.declareBinding(binding);

        // 4. 创建回调类（接收所有topic的消息）
        GlobalMqCallback callback = new GlobalMqCallback(clientId, rabbitMqHandlerMap);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(callback);

        // 5. 创建容器并监听队列
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(factory);
        container.setQueueNames(allTopicQueue.getName());
        container.setMessageListener(callback);
        container.setConcurrentConsumers(3); // 高并发处理
        return container;
    }

    /**
     * 断开所有客户端连接（服务关闭时调用）
     */
    @PreDestroy
    public void disconnect() {
        listenerContainerMap.values().forEach(container -> {
            if (container.isRunning()) {
                container.stop();
                log.info("MQ停止监听器容器，关闭消费者线程完成");
            }
        });
    }

    /**
     * 向RabbitMQ发送消息
     *
     * @param clientId 客户端ID
     * @param topic    订阅主题
     * @param message  消息报文
     */
    public void sendMessage(String clientId, String topic, String message) {
        RabbitTemplate rabbitTemplate = rabbitTemplateMap.get(clientId);
        if (rabbitTemplate == null) {
            throw new IllegalArgumentException("RabbitMQ客户端未初始化：" + clientId);
        }
        // 发送到全局监听的all.topic.exchange交换机，路由键为topic
        rabbitTemplate.convertAndSend(RabbitMQUtils.DEFAULT_EXCHANGE, topic, message);
    }

    /**
     * 向RabbitMQ发送消息
     *
     * @param clientId 客户端ID
     * @param exchange 交换机
     * @param topic    订阅主题
     * @param message  消息报文
     */
    public void sendMessage(String clientId, String exchange, String topic, String message) {
        RabbitTemplate rabbitTemplate = rabbitTemplateMap.get(clientId);
        if (rabbitTemplate == null) {
            throw new IllegalArgumentException("RabbitMQ客户端未初始化：" + clientId);
        }
        // 发送到全局监听的all.topic.exchange交换机，路由键为topic
        rabbitTemplate.convertAndSend(exchange, topic, message);
    }

    /**
     * 向RabbitMQ订阅消息
     *
     * @param clientId        客户端ID
     * @param topic           订阅主题
     * @param messageListener 消息监听器
     */
    public void subscribe(String clientId, String topic, MessageListener messageListener) {
        if (rabbitTemplateMap.isEmpty()) {
            throw new IllegalStateException("没有可用的MQ客户端");
        }
        RabbitTemplate rabbitTemplate = rabbitTemplateMap.get(clientId);
        if (rabbitTemplate == null) {
            throw new IllegalArgumentException("RabbitMQ客户端未初始化：" + clientId);
        }
        ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();

        // 构建唯一标识（避免重复注册）
        String cacheKey = clientId + ":" + topic;
        if (listenerContainerMap.containsKey(cacheKey)) {
            return; // 已注册过，直接返回
        }

        // 1. 创建队列（主题对应队列，持久化）
        Queue queue = QueueBuilder.durable(topic) // 队列名=topic
                .withArgument("x-dead-letter-exchange", "") // 可选：死信配置
                .build();

        // 2. 创建交换机（若未指定默认交换机，则使用直连交换机）
        String exchange = rabbitTemplate.getExchange() == null ? RabbitMQUtils.DEFAULT_EXCHANGE : rabbitTemplate.getExchange();
        DirectExchange directExchange = new DirectExchange(exchange, true, false);

        // 3. 绑定队列与交换机（路由键=topic）
        Binding binding = BindingBuilder.bind(queue).to(directExchange).with(topic);

        // 4. 注册队列、交换机、绑定关系（需手动注册到RabbitAdmin）
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.declareQueue(queue);
        admin.declareExchange(directExchange);
        admin.declareBinding(binding);

        // 5. 创建监听器容器
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(topic);
        container.setMessageListener(new MessageListenerAdapter(new org.springframework.amqp.core.MessageListener() {
            @Override
            public void onMessage(Message message) {
                messageListener.onMessage(message); // 回调处理消息
            }
        }));
        container.start();

        // 6. 缓存监听器容器
        listenerContainerMap.put(cacheKey, container);
    }

    /**
     * 注册注解方法到分发器
     */
    public void registerHandler(String clientId, String topic, String exchange, String queue, String routingKey, MqBeanMethodBO handlerBO) {
        String key = null;
        if (!"#".equals(topic)) {
            key = RabbitMQUtils.buildKey(clientId, topic);
        } else {
            key = RabbitMQUtils.buildKey(clientId, exchange, queue, routingKey);
        }
        rabbitMqHandlerMap.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(handlerBO);
        log.debug("注册RabbitMQ消息处理器：{} -> {}.{}", key, handlerBO.getBean().getClass().getSimpleName(), handlerBO.getMethod().getName());
    }

}
