//package com.maxinhai.platform.annotations;
//
//import java.lang.annotation.*;
//
///**
// * 接口调用日志记录注解
// */
//@Target(ElementType.METHOD)
//@Retention(RetentionPolicy.RUNTIME)
//@Documented
//public @interface ApiLog {
//
//    /**
//     * 接口描述
//     */
//    String description() default "";
//
//    /**
//     * 是否记录请求参数
//     */
//    boolean logParams() default true;
//
//    /**
//     * 是否记录返回结果
//     */
//    boolean logResult() default true;
//
//    /**
//     * 需要脱敏的参数字段（如密码字段）
//     */
//    String[] sensitiveParams() default {"password", "pwd"};
//
//}
