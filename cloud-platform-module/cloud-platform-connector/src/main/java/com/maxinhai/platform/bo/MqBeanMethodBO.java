package com.maxinhai.platform.bo;

import com.maxinhai.platform.annotation.MqSubscribe;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * MQ注解方法的存储BO
 */
@Data
@AllArgsConstructor
public class MqBeanMethodBO {

    // 注解所在的Bean实例
    private final Object bean;
    // 注解标注的方法
    private final Method method;
    // 注解属性（方便获取配置）
    private final MqSubscribe annotation;

}
