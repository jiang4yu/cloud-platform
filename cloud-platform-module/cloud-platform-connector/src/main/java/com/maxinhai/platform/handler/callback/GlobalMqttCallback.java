package com.maxinhai.platform.handler.callback;

import com.maxinhai.platform.bo.BeanMethodBO;
import com.maxinhai.platform.bo.ClientIdTopicBO;
import com.maxinhai.platform.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@Slf4j
public class GlobalMqttCallback implements MqttCallback {

    private final String clientId;
    private final Map<ClientIdTopicBO, Set<BeanMethodBO>> topicHandlerMap;

    public GlobalMqttCallback(String clientId, Map<ClientIdTopicBO, Set<BeanMethodBO>> topicHandlerMap) {
        this.clientId = clientId;
        this.topicHandlerMap = topicHandlerMap;
    }


    /**
     * 收到消息时触发（核心方法：处理所有Topic的消息）
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), "UTF-8");
        log.debug("[MQTT全局订阅回调]收到MQTT消息 >> Topic: {}, QoS: {}, 保留位: {}, 消息体: {}",
                topic, message.getQos(), message.isRetained(), payload);
        // 这里可以添加业务处理逻辑（如根据Topic分发给不同的处理器）
        handleMessage(topic, message);
    }

    /**
     * 消息交付完成时触发（仅QoS>0时生效）
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("[MQTT全局订阅回调]消息交付完成 >> MessageId: {}", token.getMessageId());
    }

    /**
     * 连接丢失时触发（可在这里实现重连逻辑，或依赖自动重连）
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.error("[MQTT全局订阅回调]MQTT连接丢失 >> 原因: {}", cause.getMessage(), cause);
    }

    /**
     * 分发消息给加了@MqttSubscribe注解的方法
     *
     * @param topic
     * @param message
     * @throws MqttException
     */
    public void handleMessage(String topic, MqttMessage message) {
        // 根据Topic查找处理方法
        if (!this.topicHandlerMap.containsKey(new ClientIdTopicBO(this.clientId, topic))) {
            return;
        }
        Set<BeanMethodBO> beanMethodBOSet = this.topicHandlerMap.get(new ClientIdTopicBO(this.clientId, topic));
        for (BeanMethodBO beanMethodBO : beanMethodBOSet) {
            try {
                // 反射调用处理方法（支持参数：String(消息体)、MqttMessage(原始消息)）
                Method method = beanMethodBO.getMethod();
                Class<?>[] parameterTypes = method.getParameterTypes();
                Object[] args = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (parameterTypes[i] == MqttMessage.class) {
                        args[i] = message;
                    }
                }
                method.invoke(beanMethodBO.getBean(), args);
            } catch (Exception e) {
                throw new BusinessException("[MQTT全局订阅回调]调用MQTT消息处理方法失败，Topic：" + topic, e);
            }
        }
    }

}
