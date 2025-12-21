package com.maxinhai.platform.utils;

import cn.hutool.core.util.StrUtil;

public class RabbitMQUtils {

    // 默认交换机
    public static final String DEFAULT_EXCHANGE = "default.exchange";
    // 默认队列
    public static final String DEFAULT_QUEUE = "default_queue";
    // 默认路由key
    public static final String DEFAULT_ROUTING_KEY = "#";

    /**
     * 构建映射表Key：clientId+topic
     *
     * @param clientId 客户端ID
     * @param topic    通道标识
     * @return 映射表Key
     */
    public static String buildKey(String clientId, String topic) {
        return clientId + ":" + topic;
    }

    /**
     * 构建映射表Key
     *
     * @param clientId   客户端ID
     * @param exchange   交换机
     * @param queue      队列
     * @param routingKey 路由Key
     * @return 映射表Key
     */
    public static String buildKey(String clientId, String exchange, String queue, String routingKey) {
        String key = null;
        if (StrUtil.isNotBlank(routingKey) && !"#".equals(routingKey)) {
            // 路由key不为空，且不为#
            key = routingKey;
        } else if (StrUtil.isEmpty(exchange) && StrUtil.isEmpty(queue)) {
            // 路由key为空，交换机、队列不为空
            key = clientId + ":" + exchange + ":" + queue;
        } else {
            // 路由key、交换机、队列为空
            key = clientId + ":" + routingKey;
        }
        return key;
    }

    /**
     * 构建映射表Key
     *
     * @param clientId   客户端ID
     * @param topic      通道标识
     * @param exchange   交换机
     * @param queue      队列
     * @param routingKey 路由Key
     * @return 映射表Key
     */
    public static String buildKey(String clientId, String topic, String exchange, String queue, String routingKey) {
        String key = null;
        if (StrUtil.isNotBlank(topic)) {
            key = RabbitMQUtils.buildKey(clientId, topic);
        } else {
            key = RabbitMQUtils.buildKey(clientId, exchange, queue, routingKey);
        }
        return key;
    }

}
