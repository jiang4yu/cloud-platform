//package com.maxinhai.platform.po;
//
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import org.springframework.data.mongodb.core.mapping.Document;
//
///**
// * @ClassName：SysLog
// * @Author: XinHai.Ma
// * @Date: 2025/9/3 13:28
// * @Description: 必须描述类做什么事情, 实现什么功能
// */
//@Data
//@EqualsAndHashCode(callSuper = true)
//@Document(collection = "users")
//public class SysLog extends RecordEntity{
//
//    /**
//     * 日志类型：ERROR（系统报错）、OPERATION（用户操作）
//     */
//    private String logType;
//    /**
//     * 日志级别：DEBUG/INFO/WARN/ERROR/FATAL（操作日志默认 INFO）
//     */
//    private String logLevel;
//    /**
//     * 服务名称（微服务场景下定位来源，如 order-service）
//     */
//    private String serviceName;
//
//
//}
