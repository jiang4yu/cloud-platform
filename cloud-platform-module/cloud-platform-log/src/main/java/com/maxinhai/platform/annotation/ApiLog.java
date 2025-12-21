package com.maxinhai.platform.annotation;

import java.lang.annotation.*;

/**
 * 接口日志注解
 * 标记需要记录日志的Controller方法
 */
@Target({ElementType.METHOD}) // 仅作用于方法
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
@Documented
public @interface ApiLog {
    /**
     * 接口描述（可选）
     */
    String value() default "";
}
