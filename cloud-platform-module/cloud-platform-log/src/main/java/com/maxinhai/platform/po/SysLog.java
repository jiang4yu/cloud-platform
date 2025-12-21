package com.maxinhai.platform.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "sys_log")
public class SysLog extends RecordEntity {

    /**
     * 请求地址
     */
    private String apiUrl;
    /**
     * 接口描述
     */
    private String apiDesc;
    /**
     * 请求方式
     */
    private String method;
    /**
     * 请求参数
     */
    private String params;
    /**
     * 响应结果
     */
    private String result;
    /**
     * 请求类方法
     */
    private String classMethod;
    /**
     * 执行状态
     */
    private String status;
    /**
     * 异常信息
     */
    private String exceptionMsg;
    /**
     * 耗时(ms)
     */
    private Long costTime;
    /**
     * 客户端IP
     */
    private String remoteAddr;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SysLog{");
        // 定义是否已添加字段的标记，用于处理逗号分隔
        boolean hasField = false;

        // 1. 接口地址
        if (apiUrl != null && !apiUrl.isEmpty()) {
            sb.append("apiUrl='").append(apiUrl).append('\'');
            hasField = true;
        }
        // 2. 接口描述
        if (apiDesc != null && !apiDesc.isEmpty()) {
            if (hasField) sb.append(", ");
            sb.append("apiDesc='").append(apiDesc).append('\'');
            hasField = true;
        }
        // 3. 请求方式
        if (method != null && !method.isEmpty()) {
            if (hasField) sb.append(", ");
            sb.append("method='").append(method).append('\'');
            hasField = true;
        }
        // 4. 请求参数
        if (params != null && !params.isEmpty()) {
            if (hasField) sb.append(", ");
            sb.append("params='").append(params).append('\'');
            hasField = true;
        }
        // 5. 响应结果
        if (result != null && !result.isEmpty()) {
            if (hasField) sb.append(", ");
            sb.append("result='").append(result).append('\'');
            hasField = true;
        }
        // 6. 类方法
        if (classMethod != null && !classMethod.isEmpty()) {
            if (hasField) sb.append(", ");
            sb.append("classMethod='").append(classMethod).append('\'');
            hasField = true;
        }
        // 7. 执行状态
        if (status != null && !status.isEmpty()) {
            if (hasField) sb.append(", ");
            sb.append("status='").append(status).append('\'');
            hasField = true;
        }
        // 8. 异常信息（允许空，仅当有值时打印）
        if (exceptionMsg != null && !exceptionMsg.isEmpty()) {
            if (hasField) sb.append(", ");
            sb.append("exceptionMsg='").append(exceptionMsg).append('\'');
            hasField = true;
        }
        // 9. 耗时（若costTime为Long，判断是否为null；若为int则判断是否>0）
        if (costTime != null) {
            if (hasField) sb.append(", ");
            sb.append("costTime=").append(costTime);
            hasField = true;
        }
        // 10. 客户端IP
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            if (hasField) sb.append(", ");
            sb.append("remoteAddr='").append(remoteAddr).append('\'');
            hasField = true;
        }

        sb.append('}');
        return sb.toString();
    }
}
