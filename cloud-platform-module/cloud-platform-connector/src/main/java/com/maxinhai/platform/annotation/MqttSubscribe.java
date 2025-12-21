package com.maxinhai.platform.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * MQTT订阅自定义注解
 */
@Target({ElementType.METHOD}) // 仅作用于方法
@Retention(RetentionPolicy.RUNTIME) // 运行时保留
@Documented
public @interface MqttSubscribe {
    /**
     * MQTT客户端ID
     */
    String clientId() default "";

    /**
     * 订阅的MQTT Topic（支持通配符，如sensor/#）
     */
    @AliasFor("topic")
    String value() default "";

    /**
     * 订阅的MQTT Topic（与value互为别名）
     */
    @AliasFor("value")
    String topic() default "";

    /**
     * QoS级别（默认0）
     */
    int qos() default 0;
}
