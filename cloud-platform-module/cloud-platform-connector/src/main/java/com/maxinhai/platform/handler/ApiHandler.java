package com.maxinhai.platform.handler;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maxinhai.platform.bo.ApiClientBO;
import com.maxinhai.platform.bo.ApiParamBO;
import com.maxinhai.platform.enums.ApiParamType;
import com.maxinhai.platform.enums.ConnectType;
import com.maxinhai.platform.enums.Method;
import com.maxinhai.platform.mapper.ApiConfigMapper;
import com.maxinhai.platform.mapper.ConnectConfigMapper;
import com.maxinhai.platform.po.ApiConfig;
import com.maxinhai.platform.po.ConnectConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName：ApiHandler
 * @Author: XinHai.Ma
 * @Date: 2025/8/21 11:39
 * @Description: API接口处理器
 */
@Slf4j
@Component
public class ApiHandler implements CommandLineRunner {

    @Resource
    private ConnectConfigMapper connectMapper;
    @Resource
    private ApiConfigMapper apiMapper;
    @Resource
    private OkHttpClient okHttpClient;
    // 客户端配置
    private final Map<String, ApiClientBO> cleintMap = new ConcurrentHashMap<>();

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化API客户端");
        initClients();
        log.info("API客户端初始化完成");
    }

    /**
     * 初始化全部客户端
     */
    public void initClients() {
        List<ConnectConfig> connectList = connectMapper.selectList(new LambdaQueryWrapper<ConnectConfig>()
                .select(ConnectConfig::getId, ConnectConfig::getType, ConnectConfig::getKey,
                        ConnectConfig::getIp, ConnectConfig::getPort, ConnectConfig::getUsername, ConnectConfig::getPassword)
                .eq(ConnectConfig::getType, ConnectType.API));
        if (connectList.isEmpty()) {
            return;
        }
        Map<String, ConnectConfig> connectMap = connectList.stream().collect(Collectors.toMap(ConnectConfig::getId, ConnectConfig -> ConnectConfig));
        List<String> connectIds = connectList.stream().map(ConnectConfig::getId).collect(Collectors.toList());
        List<ApiConfig> apiConfigList = apiMapper.selectList(new LambdaQueryWrapper<ApiConfig>()
                .select(ApiConfig::getConnectId, ApiConfig::getApiKey, ApiConfig::getUrl, ApiConfig::getMethod)
                .in(ApiConfig::getConnectId, connectIds));

        for (ApiConfig apiConfig : apiConfigList) {
            ApiClientBO build = ApiClientBO.build(connectMap.get(apiConfig.getConnectId()), apiConfig);
            cleintMap.putIfAbsent(build.getApiKey(), build);
        }
    }

    /**
     * 根据客户端ID调用接口
     *
     * @param clientId 客户端ID
     * @param params   接口参数
     * @return 响应体字符串
     * @throws IOException 异常
     */
    public String execute(String clientId, List<ApiParamBO> params) throws IOException {
        if (cleintMap.isEmpty()) {
            throw new IllegalStateException("没有可用的API客户端");
        }
        ApiClientBO apiClientBO = cleintMap.get(clientId);
        return execute(apiClientBO.getMethod(), apiClientBO.getUrl(), params);
    }

    /**
     * 执行HTTP请求
     *
     * @param method HTTP方法类型
     * @param url    目标URL（动态传入，如"http://localhost:9070/view/apiPost"）
     * @param params 请求参数列表（含参数名、类型、值）
     * @return 响应体字符串
     * @throws IOException 异常（含详细错误信息）
     */
    public String execute(Method method, String url, List<ApiParamBO> params) throws IOException {
        // 1. 校验必要参数
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("请求URL不能为空");
        }
        if (method == null) {
            throw new IllegalArgumentException("HTTP方法类型不能为空");
        }

        // 2. 构建请求
        Request request = buildRequest(method, url, params);
        if (request == null) {
            throw new UnsupportedOperationException("不支持的HTTP方法：" + method);
        }

        // 3. 执行请求并处理响应
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 增强错误信息：包含方法、URL、状态码、错误体
                String errorBody = response.body() != null ? response.body().string() : "无错误详情";
                throw new IOException(String.format(
                        "请求失败 [method=%s, url=%s, code=%d, error=%s]",
                        method, url, response.code(), errorBody
                ));
            }
            // 处理空响应体（避免返回null）
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        }
    }

    /**
     * 按方法类型构建请求（拆分逻辑，提高可读性）
     */
    private Request buildRequest(Method method, String url, List<ApiParamBO> params) throws IOException {
        switch (method) {
            case GET_PATH_VARIABLE:
                return buildGetPathVariableRequest(url, params);
            case GET:
                return buildGetRequest(url, params);
            case POST:
                return buildPostJsonRequest(url, params);
            case POST_FORM_DATA:
                return buildPostFormDataRequest(url, params);
            case PUT:
                return buildPutRequest(url, params);
            case DELETE:
                return buildDeleteRequest(url, params);
            default:
                return null;
        }
    }

    /**
     * 构建GET请求（路径参数，如 /api/{name}/{id}）
     * 逻辑：替换URL中的{参数名}占位符，如param.name=name → 替换{name}
     */
    private Request buildGetPathVariableRequest(String url, List<ApiParamBO> params) throws IOException {
        if (CollectionUtils.isEmpty(params)) {
            return new Request.Builder().url(url).get().build();
        }

        // 处理路径参数替换（使用StringBuilder避免不可变字符串问题）
        String processedUrl = url;
        for (ApiParamBO param : params) {
            String placeholder = "{" + param.getName() + "}"; // 匹配{name}格式的占位符
            if (!processedUrl.contains(placeholder)) {
                throw new IllegalArgumentException("URL中未找到路径参数占位符：" + placeholder);
            }
            // 路径参数编码：空格转%20，特殊字符编码
            String encodedValue = URLEncoder.encode(
                    param.getValue() != null ? param.getValue().toString() : "",
                    StandardCharsets.UTF_8.name()
            ).replace("+", "%20"); // 修复URLEncoder空格为+的问题
            processedUrl = processedUrl.replace(placeholder, encodedValue);
        }

        return new Request.Builder()
                .url(processedUrl)
                .get()
                .build();
    }

    /**
     * 构建GET请求（查询参数，如 /api?name=xxx&age=18）
     */
    private Request buildGetRequest(String url, List<ApiParamBO> params) {
        // 解析入参url，基于原URL添加查询参数（避免硬编码host/port）
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new IllegalArgumentException("URL格式无效：" + url);
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();

        // 动态添加查询参数（params为null/空时不处理）
        if (!CollectionUtils.isEmpty(params)) {
            for (ApiParamBO param : params) {
                String key = param.getName();
                String value = param.getValue() != null ? param.getValue().toString() : "";
                urlBuilder.addQueryParameter(key, value); // OkHttp自动编码查询参数
            }
        }

        return new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();
    }

    /**
     * 构建POST请求（JSON请求体）
     */
    private Request buildPostJsonRequest(String url, List<ApiParamBO> params) {
        RequestBody requestBody = null;
        if (!CollectionUtils.isEmpty(params)) {
            // 转换为Map再转JSON
            Map<String, Object> paramsMap = new HashMap<>();
            for (ApiParamBO param : params) {
                paramsMap.put(param.getName(), param.getValue());
            }
            String jsonBody = JSON.toJSONString(paramsMap);
            requestBody = RequestBody.create(
                    jsonBody,
                    MediaType.parse("application/json; charset=utf-8")
            );
        }

        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }

    /**
     * 构建POST请求（form-data，支持文件上传）
     */
    private Request buildPostFormDataRequest(String url, List<ApiParamBO> params) throws IOException {
        MultipartBody.Builder formBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if (CollectionUtils.isEmpty(params)) {
            return new Request.Builder().url(url).post(formBuilder.build()).build();
        }

        // 处理普通参数和文件参数（使用param.getName()作为key，适配后端参数名）
        for (ApiParamBO param : params) {
            String paramName = param.getName();
            Object value = param.getValue();
            if (value == null) {
                continue; // 跳过null值
            }

            if (ApiParamType.FILE.equals(param.getParamType())) {
                // 处理文件参数
                File file = getFileFromValue(value);
                if (!file.exists()) {
                    throw new IOException("文件不存在：" + file.getAbsolutePath());
                }
                // 自动识别文件类型（简单处理，可扩展为MimetypesFileTypeMap）
                MediaType mediaType = MediaType.parse("application/octet-stream");
                RequestBody fileBody = RequestBody.create(file, mediaType);
                formBuilder.addFormDataPart(paramName, file.getName(), fileBody); // 使用param.getName()作为key
            } else {
                // 处理普通参数（字符串/数值等）
                formBuilder.addFormDataPart(paramName, value.toString());
            }
        }

        return new Request.Builder()
                .url(url)
                .post(formBuilder.build())
                .build();
    }

    /**
     * 从参数值中解析File（支持File对象或文件路径字符串）
     */
    private File getFileFromValue(Object value) {
        if (value instanceof File) {
            return (File) value;
        } else if (value instanceof String) {
            return new File((String) value);
        } else {
            throw new IllegalArgumentException("文件参数值类型不支持（仅支持File或String路径）：" + value.getClass());
        }
    }

    /**
     * 构建PUT请求（JSON请求体）
     */
    private Request buildPutRequest(String url, List<ApiParamBO> params) {
        RequestBody requestBody = null;
        if (!CollectionUtils.isEmpty(params)) {
            Map<String, Object> paramsMap = new HashMap<>();
            for (ApiParamBO param : params) {
                paramsMap.put(param.getName(), param.getValue());
            }
            String jsonBody = JSON.toJSONString(paramsMap);
            requestBody = RequestBody.create(
                    jsonBody,
                    MediaType.parse("application/json; charset=utf-8")
            );
        }

        return new Request.Builder()
                .url(url)
                .put(requestBody) // 修复原代码的PUT方法错误（用put而非delete）
                .build();
    }

    /**
     * 构建DELETE请求（支持路径参数或JSON请求体）
     */
    private Request buildDeleteRequest(String url, List<ApiParamBO> params) throws IOException {
        // 处理路径参数替换（使用StringBuilder避免不可变字符串问题）
        String processedUrl = url;
        for (ApiParamBO param : params) {
            String placeholder = "{" + param.getName() + "}"; // 匹配{name}格式的占位符
            if (!processedUrl.contains(placeholder)) {
                throw new IllegalArgumentException("URL中未找到路径参数占位符：" + placeholder);
            }
            // 路径参数编码：空格转%20，特殊字符编码
            String encodedValue = URLEncoder.encode(
                    param.getValue() != null ? param.getValue().toString() : "",
                    StandardCharsets.UTF_8.name()
            ).replace("+", "%20"); // 修复URLEncoder空格为+的问题
            processedUrl = processedUrl.replace(placeholder, encodedValue);
        }

        return new Request.Builder()
                .url(processedUrl)
                .delete()
                .build();
    }

}
