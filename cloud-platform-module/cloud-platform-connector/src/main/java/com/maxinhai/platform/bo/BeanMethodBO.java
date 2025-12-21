package com.maxinhai.platform.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 内部类：存储Bean和对应的处理方法
 */
@Data
@AllArgsConstructor
public class BeanMethodBO {

    /**
     * JavaBean
     */
    private final Object bean;
    /**
     * 方法
     */
    private final Method method;

}
