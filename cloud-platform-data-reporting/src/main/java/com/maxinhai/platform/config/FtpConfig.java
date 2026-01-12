package com.maxinhai.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName：FtpConfig
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:17
 * @Description: FTP配置类（对应规范中的连接参数）
 */
@Data
@Component
@ConfigurationProperties(prefix = "ftp")
public class FtpConfig {

    // FTP服务器地址
    private String host;
    // FTP端口（默认21）
    private int port = 21;
    // 登录账号
    private String username;
    // 登录密码
    private String password;
    // 上传根路径（规范要求的服务器存储路径）
    private String remoteBasePath = "/";
    // 本地缓存路径（断点续传用）
    private String localCachePath = "./ftp-cache";
    // 字符编码（固定UTF-8）
    private String charset = "UTF-8";
    // 被动模式（必须开启）
    private boolean passiveMode = true;

}
