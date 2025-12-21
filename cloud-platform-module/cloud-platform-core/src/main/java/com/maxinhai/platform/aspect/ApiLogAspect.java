//package com.maxinhai.platform.aspect;
//
//import cn.hutool.json.JSONUtil;
//import com.maxinhai.platform.annotations.ApiLog;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.Map;
//
///**
// * 接口日志记录切面
// */
//@Slf4j
//@Aspect
//@Component
//public class ApiLogAspect {
//
//    @Pointcut("@annotation(apiLog)")
//    public void pointcut(ApiLog apiLog) {}
//
//    @Around("pointcut(apiLog)")
//    public Object around(ProceedingJoinPoint joinPoint, ApiLog apiLog) throws Throwable {
//        // 1. 记录请求信息
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        String ip = request.getRemoteAddr();
//        String method = request.getMethod(); // 请求方式（GET/POST）
//        String path = request.getRequestURI();
//        String userId = getCurrentUserId(); // 当前用户ID
//        LocalDateTime startTime = LocalDateTime.now();
//
//        // 2. 处理请求参数（脱敏敏感字段）
//        String params = "[]";
//        if (apiLog.logParams() && joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
//            params = JSONUtil.toJsonStr(desensitizeParams(joinPoint.getArgs(), apiLog.sensitiveParams()));
//        }
//
//        // 3. 执行接口方法
//        Object result = null;
//        String resultStr = "null";
//        try {
//            result = joinPoint.proceed();
//            // 4. 处理返回结果
//            if (apiLog.logResult() && result != null) {
//                resultStr = JSONUtil.toJsonStr(result);
//            }
//            return result;
//        } finally {
//            // 5. 计算耗时并记录日志
//            long cost = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
//            log.info(
//                    "接口日志 - 描述:{} | 用户:{} | IP:{} | 方法:{} | 路径:{} | 耗时:{}ms | 参数:{} | 结果:{}",
//                    apiLog.description(), userId, ip, method, path, cost, params, resultStr
//            );
//            // 实际场景可保存到数据库：如ApiLog实体类 -> 调用日志DAO存储
//        }
//    }
//
//    // 参数脱敏（替换敏感字段为***）
//    private Object desensitizeParams(Object[] args, String[] sensitiveParams) {
//        if (args.length == 0 || sensitiveParams.length == 0) {
//            return args;
//        }
//        // 简化处理：仅处理Map和JavaBean（实际可扩展更多类型）
//        for (Object arg : args) {
//            if (arg instanceof Map) {
//                Map<?, ?> map = (Map<?, ?>) arg;
//                for (String param : sensitiveParams) {
//                    if (map.containsKey(param)) {
//                        ((Map) map).put(param, "***");
//                    }
//                }
//            } else {
//                // 反射处理JavaBean（略，可使用Hutool的BeanUtil实现）
//            }
//        }
//        return args;
//    }
//
//    // 模拟获取当前用户ID
//    private String getCurrentUserId() {
//        return "default_user";
//    }
//
//}
