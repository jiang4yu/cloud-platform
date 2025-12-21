package com.maxinhai.platform.handler.callback;

import com.maxinhai.platform.bo.MqBeanMethodBO;
import com.maxinhai.platform.utils.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQ消息回调类：仅负责接收消息，不处理业务逻辑
 */
@Slf4j
public class GlobalMqCallback implements MessageListener {

    // 客户端ID（对应RabbitTemplate的clientId）
    private final String clientId;
    //
    private final Map<String, Set<MqBeanMethodBO>> rabbitMqHandlerMap;

    public GlobalMqCallback(String clientId, Map<String, Set<MqBeanMethodBO>> rabbitMqHandlerMap) {
        this.clientId = clientId;
        this.rabbitMqHandlerMap = rabbitMqHandlerMap;
    }

    @Override
    public void onMessage(Message message) {
        log.debug("RabbitMQ回调接收消息：clientId={}, payload={}",
                this.clientId, new String(message.getBody(), StandardCharsets.UTF_8));
        // 转发给分发器处理
        dispatch(clientId, message);
    }

    /**
     * 分发消息到注解方法
     */
    public void dispatch(String clientId, Message message) {
        // 1. 获取消息的路由键（对应你代码中的topic）
        String receivedRoutingKey = message.getMessageProperties().getReceivedRoutingKey();
        // 2. 获取消费的队列名
        String consumerQueue = message.getMessageProperties().getConsumerQueue();
        // 3. 获取消息来自的交换机
        String receivedExchange = message.getMessageProperties().getReceivedExchange();

        log.debug("RabbitMQ回调接收消息：clientId={}, 实际路由键={}, 消费队列={}, 交换机={}",
                clientId, receivedRoutingKey, consumerQueue, receivedExchange);

        String key = RabbitMQUtils.buildKey(clientId, receivedRoutingKey);

        // 订阅全部的
        Set<MqBeanMethodBO> handlerBOs = rabbitMqHandlerMap.getOrDefault(clientId + ":#", ConcurrentHashMap.newKeySet());
        // 指定通配符
        if (rabbitMqHandlerMap.containsKey(key)) {
            handlerBOs.addAll(rabbitMqHandlerMap.get(key));
        }

        if (CollectionUtils.isEmpty(handlerBOs)) {
            log.warn("未找到匹配的RabbitMQ处理器：{}", key);
            return;
        }

        // 遍历所有注解方法，反射调用
        for (MqBeanMethodBO beanMethod : handlerBOs) {
            try {
                invokeHandlerMethod(beanMethod, message);
            } catch (Exception e) {
                log.error("调用RabbitMQ处理器失败：{}", key, e);
            }
        }
    }

    /**
     * 反射调用注解方法
     */
    private void invokeHandlerMethod(MqBeanMethodBO beanMethod, Message message) throws Exception {
        Method method = beanMethod.getMethod();
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = buildMethodArgs(paramTypes, message);
        method.invoke(beanMethod.getBean(), args); // 执行注解方法
        log.debug("执行RabbitMQ处理器成功：{}.{}", beanMethod.getBean().getClass().getSimpleName(), method.getName());
    }

    /**
     * 构建方法入参（支持String/Message/自定义对象）
     */
    private Object[] buildMethodArgs(Class<?>[] paramTypes, Message message) {
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];
            if (type == Message.class) {
                args[i] = message;
            }
        }
        return args;
    }

}
