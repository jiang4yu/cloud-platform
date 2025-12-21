package com.maxinhai.platform.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maxinhai.platform.annotation.MqttSubscribe;
import com.maxinhai.platform.bo.BeanMethodBO;
import com.maxinhai.platform.bo.ClientIdTopicBO;
import com.maxinhai.platform.enums.ConnectType;
import com.maxinhai.platform.exception.BusinessException;
import com.maxinhai.platform.handler.callback.GlobalMqttCallback;
import com.maxinhai.platform.handler.callback.MqttMsgCallback;
import com.maxinhai.platform.mapper.ConnectConfigMapper;
import com.maxinhai.platform.mapper.MqttConfigMapper;
import com.maxinhai.platform.po.ConnectConfig;
import com.maxinhai.platform.po.MqttConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName：MqttHandler
 * @Author: XinHai.Ma
 * @Date: 2025/8/21 11:39
 * @Description: MQTT消息处理器
 */
@Slf4j
@Component
public class MqttHandler implements CommandLineRunner {

    @Resource
    private ConnectConfigMapper connectMapper;
    @Resource
    private MqttConfigMapper mqttMapper;
    @Resource
    private ApplicationContext applicationContext;

    // 全局订阅的Topic（匹配所有层级的Topic）
    private static final String GLOBAL_TOPIC = "/topic/#";
    // 默认QoS级别（0/1/2，根据业务需求调整）
    private static final int DEFAULT_QOS = 0;
    // 存储Topic与处理方法的映射：key=clientId+topic，value=Method+Bean
    public static final Map<ClientIdTopicBO, Set<BeanMethodBO>> topicHandlerMap = new ConcurrentHashMap<>();

    // MQTT客户端缓存（客户端ID->客户端实例）
    private static final ConcurrentHashMap<String, MqttClient> clientMap = new ConcurrentHashMap<>();
    // MQTT回调端缓存（客户端ID->回调实例）
    private static final ConcurrentHashMap<String, MqttCallback> callbackMap = new ConcurrentHashMap<>();

    @Override
    public void run(String... args) throws Exception {
        scanMqttSubscribeAnnotations();

        log.info("开始初始化MQTT客户端");
        initClients();
        log.info("MQTT客户端初始化完成");

        // 监听MQTT心跳主题
        receiveMessage("emqx", "/topic/heartbeat", new MqttMsgCallback() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                log.info("MQTT收到消息 - 主题: {}, 内容: {}", topic, payload);
            }
        });
    }

    /**
     * 初始化：扫描所有带有@MqttSubscribe的方法
     */
    public void scanMqttSubscribeAnnotations() throws BeansException {
        // 扫描所有Spring Bean
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            // 获取Bean的所有方法
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                // 检查方法是否带有@MqttSubscribe注解
                MqttSubscribe annotation = method.getAnnotation(MqttSubscribe.class);
                if (annotation != null) {
                    String clientId = annotation.clientId();
                    if (clientId.isEmpty()) {
                        throw new BusinessException("MQTT ClientId不能为空，方法：" + method.getName());
                    }
                    String topic = annotation.topic().isEmpty() ? annotation.value() : annotation.topic();
                    if (topic.isEmpty()) {
                        throw new BusinessException("MQTT Topic不能为空，方法：" + method.getName());
                    }
                    // 注册Topic与处理方法的映射
                    topicHandlerMap.computeIfAbsent(new ClientIdTopicBO(clientId, topic), k -> new HashSet<>()).add(new BeanMethodBO(bean, method));
                }
            }
        }
    }

    /**
     * 初始化全部客户端
     *
     * @throws MqttException MQTT异常
     */
    public void initClients() throws MqttException {
        // 查询连接配置
        List<ConnectConfig> connectList = connectMapper.selectList(new LambdaQueryWrapper<ConnectConfig>()
                .select(ConnectConfig::getId, ConnectConfig::getKey, ConnectConfig::getType,
                        ConnectConfig::getIp, ConnectConfig::getPort,
                        ConnectConfig::getUsername, ConnectConfig::getPassword)
                .eq(ConnectConfig::getType, ConnectType.MQTT));
        if (CollectionUtils.isEmpty(connectList)) {
            return;
        }

        // 查询MQTT配置
        List<String> connectIds = connectList.stream().map(ConnectConfig::getId).collect(Collectors.toList());
        List<MqttConfig> mqttConfigList = mqttMapper.selectList(new LambdaQueryWrapper<MqttConfig>()
                .select(MqttConfig::getId, MqttConfig::getConnectId, MqttConfig::getTopic, MqttConfig::getQos)
                .in(MqttConfig::getConnectId, connectIds));
        Map<String, List<MqttConfig>> mqttConfigMap = mqttConfigList.stream().collect(Collectors.groupingBy(MqttConfig::getConnectId));

        // 初始化客户端和回调
        for (ConnectConfig connectConfig : connectList) {
            MqttClient mqttClient = initClient(connectConfig, mqttConfigMap.get(connectConfig.getId()));
            clientMap.putIfAbsent(connectConfig.getKey(), mqttClient);
        }
    }

    /**
     * 初始化客户端
     *
     * @param connectConfig  连接配置
     * @param mqttConfigList MQTT配置集合
     * @return MQTT客户端
     * @throws MqttException MQTT异常
     */
    public MqttClient initClient(ConnectConfig connectConfig, List<MqttConfig> mqttConfigList) throws MqttException {
        // 1. 构建连接URL（tcp://host:port）
        String serverURI = "tcp://" + connectConfig.getIp() + ":" + connectConfig.getPort();

        // 2. 创建客户端实例
        MqttClient client = new MqttClient(serverURI, connectConfig.getKey());

        // 3. 配置连接参数
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(connectConfig.getUsername());
        options.setPassword(connectConfig.getPassword() != null ? connectConfig.getPassword().toCharArray() : null);
        options.setKeepAliveInterval(60); // 心跳时间（秒）
        options.setCleanSession(true); // 是否清理会话
        options.setAutomaticReconnect(true); // 启用自动重连
        options.setConnectionTimeout(30); // 连接超时时间（秒）
        options.setAutomaticReconnect(true); // 开启自动重连
        options.setMaxReconnectDelay(10000); // 最大重连延迟（毫秒）：避免重连过于频繁，默认128000

        // 4. 设置消息回调
        client.setCallback(new GlobalMqttCallback(connectConfig.getKey(), topicHandlerMap));

        // 5. 建立连接
        if (!client.isConnected()) {
            try {
                client.connect(options);
                log.info("MQTT客户端连接成功（clientId: {}, 服务器: {}）", connectConfig.getKey(), serverURI);
            } catch (MqttException e) {
                log.error("MQTT客户端连接出错：{}", e.getMessage());
                throw new RuntimeException(e);
            }
        }

        // 6. 订阅主题
        client.subscribe(GLOBAL_TOPIC, DEFAULT_QOS);
        for (MqttConfig mqttConfig : mqttConfigList) {
            client.subscribe(mqttConfig.getTopic(), mqttConfig.getQos());
            log.info("已订阅主题（clientId: {}, 主题: {}, QoS: {}）", connectConfig.getKey(), mqttConfig.getTopic(), mqttConfig.getQos());
        }
        return client;
    }

    /**
     * 断开所有客户端连接（服务关闭时调用）
     */
    @PreDestroy
    public void disconnect() {
        clientMap.values().forEach(client -> {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                    log.info("MQTT客户端已断开（clientId: {}）", client.getClientId());
                }
            } catch (MqttException e) {
                log.error("断开客户端连接失败（clientId: {}）", client.getClientId(), e);
            }
        });
    }

    /**
     * 发送MQTT消息
     *
     * @param clientId 客户ID
     * @param topic    订阅主题
     * @param payload  发送报文
     * @param qos      QoS等级（0/1/2）
     * @throws MqttException MQTT异常
     */
    public void sendMessage(String clientId, String topic, String payload, int qos) throws MqttException {
        // 1. 检查客户端是否存在
        if (clientMap.isEmpty()) {
            throw new IllegalStateException("没有可用的MQTT客户端");
        }
        MqttClient client = clientMap.get(clientId);
        if (client == null) {
            throw new IllegalArgumentException("未找到客户端（clientId: " + clientId + "）");
        }

        // 2. 检查客户端是否连接
        if (!client.isConnected()) {
            throw new IllegalStateException("客户端未连接（clientId: " + clientId + "）");
        }

        // 3. 构建消息
        MqttMessage message = new MqttMessage();
        message.setPayload(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(qos);
        // 可选：设置消息是否保留（服务器会保留最后一条消息，新订阅者会收到）
        message.setRetained(false);

        // 4. 发送消息
        client.publish(topic, message);
        log.info("消息发送成功（clientId: {}, 主题: {}, 内容: {}）", clientId, topic, payload);
    }

    /**
     * @param clientId 客户ID
     * @param topic    订阅主题
     * @param payload  发送报文
     * @throws MqttException MQTT异常
     */
    public void sendMessage(String clientId, String topic, String payload) throws MqttException {
        sendMessage(clientId, topic, payload, 0);
    }

    /**
     * 接收消息
     *
     * @param topic 订阅主题
     */
    public void receiveMessage(String clientId, String topic, MqttCallback callback) throws MqttException {
        if (clientMap.isEmpty()) {
            throw new IllegalStateException("没有可用的MQTT客户端");
        }
        MqttClient client = clientMap.get(clientId);
        if (client == null) {
            throw new IllegalArgumentException("未找到客户端（clientId: " + clientId + "）");
        }
        MqttCallback mqttCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                log.error("MQTT连接断开，原因: {}", throwable.getMessage(), throwable);
            }

            @Override
            public void messageArrived(String topic1, MqttMessage message) throws Exception {
                if (topic1.equals(topic)) {
                    callback.messageArrived(topic, message);
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    log.info("mqtt收到消息 - 主题: {}, 内容: {}", topic, payload);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        };
        client.setCallback(mqttCallback);
        callbackMap.put(clientId, mqttCallback);
    }

}
