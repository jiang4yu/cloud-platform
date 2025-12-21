package com.maxinhai.platform.annotation;

import org.springframework.amqp.core.ExchangeTypes;

import java.lang.annotation.*;

/**
 * MQ订阅自定义注解
 */
@Target({ElementType.METHOD}) // 仅作用于方法
@Retention(RetentionPolicy.RUNTIME) // 运行时保留
@Documented
public @interface MqSubscribe {

    /**
     * 客户端ID（对应RabbitMQ的clientId，必填）
     */
    String clientId();

    /** 交换机名称（缺省为 "default.exchange"） */
    String exchange() default "default.exchange";

    /** 交换机类型（缺省为Topic，支持Direct/Topic/Fanout） */
    String exchangeType() default ExchangeTypes.TOPIC;

    /** 队列名称（缺省为 clientId + ":" + topic） */
    String queue() default "default_queue";

    /**
     * 订阅主题（对应RabbitMQ的队列/路由键，必填）
     */
    String routingKey() default "#";

    /**
     * 是否自动确认消息（默认true）
     */
    boolean autoAck() default true;

    /**
     * 消费者并发数（默认1）
     */
    int concurrency() default 1;

}
