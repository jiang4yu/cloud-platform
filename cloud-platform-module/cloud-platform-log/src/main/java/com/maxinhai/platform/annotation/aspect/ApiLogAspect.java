package com.maxinhai.platform.annotation.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxinhai.platform.annotation.ApiLog;
import com.maxinhai.platform.po.SysLog;
import com.maxinhai.platform.repository.SysLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 接口日志切面
 * 处理@ApiLog注解，记录接口调用日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {

    // JSON序列化工具
    private final ObjectMapper objectMapper;
    private final SysLogRepository sysLogRepository;
    private final List<SysLog> logList = new ArrayList<>(1000);

    /**
     * 切入点：匹配所有标注@ApiLog的方法
     */
    @Pointcut("@annotation(com.maxinhai.platform.annotation.ApiLog)")
    public void apiLogPointcut() {
    }

    /**
     * 环绕通知：捕获接口调用信息并记录日志
     */
    @Around("apiLogPointcut()")
    public Object aroundApiLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取请求上下文
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 2. 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ApiLog apiLog = method.getAnnotation(ApiLog.class);
        String apiDesc = apiLog.value();

        // 3. 构建基础日志信息
        SysLog sysLog = new SysLog();
        sysLog.setApiUrl(request.getRequestURI());
        sysLog.setApiDesc(apiDesc);
        sysLog.setMethod(request.getMethod());
        sysLog.setClassMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        sysLog.setRemoteAddr(request.getRemoteAddr());

        // 4. 处理请求参数（排除文件类型参数，避免序列化异常）
        Object[] args = joinPoint.getArgs();
        Map<String, Object> paramMap = new HashMap<>();
        String[] parameterNames = signature.getParameterNames();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            // 跳过文件类型参数（MultipartFile）和HttpServletRequest
            if (arg instanceof MultipartFile || arg instanceof HttpServletRequest) {
                continue;
            }
            paramMap.put(parameterNames[i], arg);
        }
        sysLog.setParams(objectMapper.writeValueAsString(paramMap));

        // 5. 记录接口开始时间，执行目标方法
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed(); // 执行接口方法
            return result;
        } catch (Throwable e) {
            sysLog.setStatus("失败");
            sysLog.setExceptionMsg(e.getMessage());
            throw e; // 抛出异常，不影响原有业务逻辑
        } finally {
            // 6. 计算耗时，补全日志并输出
            long costTime = System.currentTimeMillis() - startTime;
            sysLog.setStatus("成功");
            sysLog.setCostTime(costTime);
            sysLog.setResult(result != null ? objectMapper.writeValueAsString(result) : "null");

            // 输出结构化日志（可根据需求调整格式，如JSON、ELK适配等）
            log.debug("接口调用日志：{}", objectMapper.writeValueAsString(sysLog));

            addLog(sysLog);
        }
    }

    /**
     * 添加日志到集合，日志数量到达1000条持久到数据库
     *
     * @param sysLog 接口日志
     */
    private void addLog(SysLog sysLog) {
        logList.add(sysLog);
        if (logList.size() >= 1000) {
            sysLogRepository.saveAll(logList);
            logList.clear();
        }
    }

}
