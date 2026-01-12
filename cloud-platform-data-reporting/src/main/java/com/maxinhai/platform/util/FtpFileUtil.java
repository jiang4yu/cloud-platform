package com.maxinhai.platform.util;

import com.maxinhai.platform.config.FtpConfig;
import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.enums.FtpFileTypeEnum;
import com.maxinhai.platform.parser.DataParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/**
 * @ClassName：FtpFileUtil
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:13
 * @Description: FTP文件生成+上传核心工具类（支持12类报文）
 */
@Slf4j
@Component
public class FtpFileUtil {

    @Resource
    private FtpConfig ftpConfig;
    private final Random random = new Random();

    /**
     * 生成指定类型的FTP报文文件（本地缓存）
     *
     * @param coalMineCode 煤矿编码（12位）
     * @param mineType     矿井类型（1=井工矿，2=露天矿）
     * @param fileType     报文类型
     * @param dataList     数据列表
     * @param parser       数据解析器
     * @return 本地文件路径
     */
    public <T> String generateFtpFile(
            String coalMineCode,
            int mineType,
            FtpFileTypeEnum fileType,
            List<T> dataList,
            DataParser<T> parser
    ) throws IOException {
        // 1. 校验煤矿编码
        if (!FtpFieldCheckUtil.checkCoalMineCode(coalMineCode)) {
            throw new IllegalArgumentException("煤矿编码不符合要求（必须12位）：" + coalMineCode);
        }

        // 2. 构建文件名（严格遵循规范）
        String timestamp = new DateTime().toString(FtpConstant.DATE_FORMAT);
        String randomNum = String.format("%0" + FtpConstant.RANDOM_NUM_LENGTH + "d", random.nextInt(9999));
        String fileName = String.format("%s_%d_%s_%s_%s%s",
                coalMineCode, mineType, fileType.getCode(), timestamp, randomNum, FtpConstant.FILE_SUFFIX);

        // 3. 构建文件内容
        StringBuilder content = new StringBuilder();
        // 3.1 文件头
        String header = String.join(FtpConstant.FIELD_SEPARATOR,
                coalMineCode,
                "陕西省XX煤矿", // 煤矿名称（实际替换为真实名称）
                String.valueOf(mineType),
                timestamp
        ) + FtpConstant.RECORD_END;
        content.append(header);

        // 3.2 文件体
        if (dataList == null || dataList.isEmpty()) {
            log.error("数据列表为空，无法生成有效FTP文件");
            throw new IllegalArgumentException("数据列表不能为空");
        }
        for (T data : dataList) {
            String record = parser.parse(data);
            content.append(record).append(FtpConstant.RECORD_END).append(FtpConstant.GROUP_END);
        }

        // 4. 校验文件内容（杜绝空内容）
        String contentStr = content.toString();
        if (contentStr.isBlank()) { // Java 11新增的isBlank()，比isEmpty()更友好（能识别全空格）
            log.error("FTP文件内容为空，禁止生成空文件");
            throw new RuntimeException("FTP文件内容为空，无法生成有效文件");
        }

        // 5. 确保缓存目录存在
        File cacheDir = new File(ftpConfig.getLocalCachePath());
        if (!cacheDir.exists()) {
            boolean mkdir = cacheDir.mkdirs();
            if (!mkdir) {
                throw new RuntimeException("创建本地缓存目录失败：" + ftpConfig.getLocalCachePath());
            }
        }

        // 6. 写入本地文件（Java 11 NIO.2 API）
        Path localFilePath = Paths.get(cacheDir.getAbsolutePath(), fileName);
        try {
            // Files.write() 自动处理流的打开/关闭，且默认刷新缓冲区（无需手动flush）
            Files.write(localFilePath, contentStr.getBytes(FtpFieldCheckUtil.getCharset(ftpConfig.getCharset())));
        } catch (IOException e) {
            log.error("生成FTP文件失败，路径：{}", localFilePath.toAbsolutePath(), e);
            throw new RuntimeException("文件生成异常", e);
        }

        // 7. 获取真实文件大小（Java 11中Files.size()比File.length()更稳定）
        long fileSize = Files.size(localFilePath);
        // 8. 记录文件生成日志并返回路径
        log.info("FTP报文文件生成成功：{}，大小：{}字节", localFilePath.toAbsolutePath(), fileSize);
        return localFilePath.toAbsolutePath().toString();
    }

//    /**
//     * 上传FTP文件到服务器（支持断点续传）
//     *
//     * @param localFilePath 本地文件路径
//     * @return 上传结果
//     */
//    public boolean uploadFtpFile(String localFilePath) {
//        FTPClient ftpClient = new FTPClient();
//        File localFile = new File(localFilePath);
//        if (!localFile.exists()) {
//            log.error("本地文件不存在：{}", localFilePath);
//            return false;
//        }
//
//        // 校验文件大小是否为0
//        if (localFile.length() == 0) {
//            log.error("本地文件大小为0字节，取消上传：{}", localFilePath);
//            return false;
//        }
//
//        try {
//            // 1. 连接FTP
//            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
//            boolean login = ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
//            int reply = ftpClient.getReplyCode();
//            if (!login || !FTPReply.isPositiveCompletion(reply)) {
//                log.error("FTP登录失败，响应码：{}", reply);
//                return false;
//            }
//
//            // 2. FTP配置
//            ftpClient.setControlEncoding(ftpConfig.getCharset());
//            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
//            if (ftpConfig.isPassiveMode()) {
//                ftpClient.enterLocalPassiveMode();
//            }
//
//            // 3. 切换/创建远程目录
//            if (!ftpClient.changeWorkingDirectory(ftpConfig.getRemoteBasePath())) {
//                boolean mkdir = ftpClient.makeDirectory(ftpConfig.getRemoteBasePath());
//                if (!mkdir) {
//                    log.error("创建远程目录失败：{}", ftpConfig.getRemoteBasePath());
//                    return false;
//                }
//                ftpClient.changeWorkingDirectory(ftpConfig.getRemoteBasePath());
//            }
//
//            // 4. 断点续传
//            long skipSize = 0;
//            String remoteFileName = localFile.getName();
//            if (isFileExists(ftpClient, remoteFileName)) {
//                skipSize = ftpClient.size(remoteFileName);
//                log.info("断点续传：已上传{}字节，跳过该部分", skipSize);
//            }
//
//            // 5. 上传文件
//            try (InputStream in = new FileInputStream(localFile)) {
//                if (skipSize > 0) {
//                    in.skip(skipSize);
//                    ftpClient.setRestartOffset(skipSize);
//                }
//                boolean success = ftpClient.storeFile(remoteFileName, in);
//                if (success) {
//                    log.info("文件上传成功：{} -> {}", localFilePath, ftpConfig.getRemoteBasePath() + "/" + remoteFileName);
//                    // 上传成功删除本地文件
//                    localFile.delete();
//                    return true;
//                } else {
//                    log.error("上传失败，FTP响应：{}", ftpClient.getReplyString());
//                    return false;
//                }
//            }
//        } catch (IOException e) {
//            log.error("FTP上传异常", e);
//            return false;
//        } finally {
//            // 关闭连接
//            if (ftpClient.isConnected()) {
//                try {
//                    ftpClient.logout();
//                    ftpClient.disconnect();
//                } catch (IOException e) {
//                    log.error("关闭FTP连接失败", e);
//                }
//            }
//        }
//    }

    /**
     * 上传FTP文件到服务器（支持断点续传，FTP服务器需要设置-保留部分上载：true）
     *
     * @param localFilePath 本地文件路径
     * @return 上传结果
     */
    public boolean uploadFtpFile(String localFilePath) {
        FTPClient ftpClient = new FTPClient();
        File localFile = new File(localFilePath);

        // 前置校验：本地文件必须存在且有内容
        if (!localFile.exists()) {
            log.error("本地文件不存在：{}", localFilePath);
            return false;
        }
        long localFileSize = localFile.length();
        if (localFileSize == 0) {
            log.error("本地文件大小为0字节，禁止上传：{}", localFilePath);
            return false;
        }
        log.info("准备上传文件：{}，本地大小：{}字节", localFile.getName(), localFileSize);

        FileInputStream fis = null;
        try {
            // 1. 连接FTP并强制配置（IIS FTP必需）
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
            boolean loginSuccess = ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            if (!loginSuccess) {
                log.error("FTP登录失败，响应码：{}", ftpClient.getReplyCode());
                return false;
            }

            // 核心：强制二进制模式（IIS FTP上传非文本文件必须用二进制，否则内容丢失）
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 强制被动模式（IIS FTP默认要求）
            ftpClient.enterLocalPassiveMode();
            // 增大缓冲区（适配100KB+文件）
            ftpClient.setBufferSize(1024*1024);
            // 适配内网被动模式IP
            ftpClient.setPassiveNatWorkaround(true);
            // 禁用ASCII转换（避免内容被修改）
            ftpClient.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
            ftpClient.setControlEncoding(ftpConfig.getCharset());

            // 2. 切换远程目录（原有逻辑不变）
            String remotePath = ftpConfig.getRemoteBasePath().trim();
            boolean isDirExist = ftpClient.changeWorkingDirectory(remotePath);
            if (!isDirExist) {
                String[] dirSegments = remotePath.split("/");
                StringBuilder currentPath = new StringBuilder();
                for (String segment : dirSegments) {
                    if (segment.isEmpty()) continue;
                    currentPath.append("/").append(segment);
                    String dir = currentPath.toString();
                    if (!ftpClient.changeWorkingDirectory(dir) && !ftpClient.makeDirectory(dir)) {
                        log.error("创建目录失败：{}", dir);
                        return false;
                    }
                }
            }

            // 3. 处理远程旧文件（断点续传修复逻辑）
            String remoteFileName = localFile.getName();
            if (isFileExists(ftpClient, remoteFileName)) {
                long remoteFileSize = ftpClient.size(remoteFileName);
                if (remoteFileSize > localFileSize) {
                    // 删除更大的旧文件，避免断点续传参数错误
                    boolean deleteSuccess = ftpClient.deleteFile(remoteFileName);
                    if (!deleteSuccess) {
                        log.error("删除远程旧文件失败：{}", remoteFileName);
                        return false;
                    }
                    log.info("已删除远程旧文件：{}", remoteFileName);
                }
            }

            // 4. 核心：上传文件并校验传输结果
            fis = new FileInputStream(localFile);
            // 关键：使用storeFile（全量上传），并校验返回值
            boolean uploadSuccess = ftpClient.storeFile(remoteFileName, fis);
            // 额外校验：获取FTP响应码，确认上传完成
            int replyCode = ftpClient.getReplyCode();
            log.info("上传响应码：{}，上传结果：{}", replyCode, uploadSuccess);

            // 5. 校验远程文件大小（核心：确认内容已写入）
            if (uploadSuccess && FTPReply.isPositiveCompletion(replyCode)) {
                // 重新获取远程文件大小
                long remoteNewSize = ftpClient.size(remoteFileName);
                log.info("本地文件大小：{}字节，远程文件大小：{}字节", localFileSize, remoteNewSize);
                if (remoteNewSize != localFileSize) {
                    log.error("上传内容不完整！本地{}字节，远程{}字节", localFileSize, remoteNewSize);
                    return false;
                }
                log.info("文件上传成功，内容完整：{}", remoteFileName);
                return true;
            } else {
                String replyMsg = ftpClient.getReplyString();
                log.error("上传失败，响应信息：{}", replyMsg);
                return false;
            }

        } catch (IOException e) {
            log.error("FTP上传异常", e);
            return false;
        } finally {
            // 关键：先关闭文件输入流，再关闭FTP连接（避免流未刷盘）
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("关闭文件输入流失败", e);
                }
            }
            // 关闭FTP连接
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    log.error("关闭FTP连接失败", e);
                }
            }
        }
    }

    /**
     * 纯全量上传FTP文件（无断点续传，FTP服务器需要设置-保留部分上载：false）
     * @param localFilePath 本地文件绝对路径
     * @return 上传成功返回true，失败返回false
     */
    public boolean uploadSmallFile(String localFilePath) {
        // 1. 前置校验：本地文件必须存在且非空（小文件核心校验）
        File localFile = new File(localFilePath);
        if (!localFile.exists()) {
            log.error("本地文件不存在，上传失败：{}", localFilePath);
            return false;
        }
        long localFileSize = localFile.length();
        if (localFileSize == 0) {
            log.error("本地文件为空（0字节），禁止上传：{}", localFilePath);
            return false;
        }
        if (localFileSize > 1024 * 100) { // 限制仅上传＜100KB的小文件
            log.warn("文件大小{}字节（超过100KB），建议使用断点续传方法", localFileSize);
        }
        log.info("开始全量上传小文件：{}，大小：{}字节", localFile.getName(), localFileSize);

        FTPClient ftpClient = new FTPClient();
        FileInputStream fis = null;

        try {
            // 2. 连接FTP服务器（IIS FTP必需配置）
            // 设置连接超时（小文件建议短超时，5秒）
            ftpClient.setConnectTimeout(5000);
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
            log.info("FTP服务器连接成功：{}:{}", ftpConfig.getHost(), ftpConfig.getPort());

            // 3. 登录FTP（校验登录结果）
            boolean loginSuccess = ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            int loginReplyCode = ftpClient.getReplyCode();
            if (!loginSuccess || !FTPReply.isPositiveCompletion(loginReplyCode)) {
                log.error("FTP登录失败，响应码：{}，请检查账号密码/IP白名单", loginReplyCode);
                return false;
            }
            log.info("FTP登录成功，账号：{}", ftpConfig.getUsername());

            // 4. IIS FTP核心配置（小文件上传必须）
            ftpClient.setControlEncoding(ftpConfig.getCharset()); // 编码和服务器一致
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 强制二进制模式（避免内容丢失）
            ftpClient.enterLocalPassiveMode(); // 被动模式（IIS FTP默认要求）
            ftpClient.setBufferSize(1024*1024); // 增大缓冲区（适配100KB+文件）
            ftpClient.setPassiveNatWorkaround(true); // 适配内网被动模式IP
            ftpClient.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE); // 流模式适配小文件

            // 5. 切换/创建远程目录（确保目录存在）
            String remoteBasePath = ftpConfig.getRemoteBasePath().trim();
            if (!ftpClient.changeWorkingDirectory(remoteBasePath)) {
                // 逐级创建目录（适配多级目录）
                String[] dirs = remoteBasePath.split("/");
                StringBuilder tempPath = new StringBuilder();
                for (String dir : dirs) {
                    if (dir.isEmpty()) continue;
                    tempPath.append("/").append(dir);
                    String currentDir = tempPath.toString();
                    if (!ftpClient.changeWorkingDirectory(currentDir)) {
                        boolean mkdirSuccess = ftpClient.makeDirectory(currentDir);
                        if (!mkdirSuccess) {
                            log.error("创建远程目录失败：{}", currentDir);
                            return false;
                        }
                        ftpClient.changeWorkingDirectory(currentDir);
                    }
                }
                log.info("远程目录创建/切换成功：{}", remoteBasePath);
            }

            // 6. 全量上传文件（核心：无断点续传，直接覆盖旧文件）
            fis = new FileInputStream(localFile);
            String remoteFileName = localFile.getName();
            // storeFile：全量上传，直接覆盖服务器同名文件（IIS FTP默认允许覆盖）
            boolean uploadSuccess = ftpClient.storeFile(remoteFileName, fis);
            int uploadReplyCode = ftpClient.getReplyCode();

            // 7. 校验上传结果（核心：确认内容完整）
            if (uploadSuccess && FTPReply.isPositiveCompletion(uploadReplyCode)) {
                // 校验远程文件大小（确保内容不是空的）
                long remoteFileSize = ftpClient.size(remoteFileName);
                if (remoteFileSize == localFileSize) {
                    log.info("小文件全量上传成功！本地大小：{}字节，远程大小：{}字节",
                            localFileSize, remoteFileSize);
                    return true;
                } else {
                    log.error("上传内容不完整！本地{}字节，远程{}字节", localFileSize, remoteFileSize);
                    // 尝试删除上传失败的空文件
                    ftpClient.deleteFile(remoteFileName);
                    return false;
                }
            } else {
                String replyMsg = ftpClient.getReplyString();
                log.error("小文件上传失败，FTP响应码：{}，响应信息：{}", uploadReplyCode, replyMsg);
                return false;
            }

        } catch (IOException e) {
            log.error("小文件FTP上传异常", e);
            return false;
        } finally {
            // 8. 资源释放（先关文件流，再关FTP连接）
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("关闭本地文件流失败", e);
                }
            }
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    log.error("关闭FTP连接失败", e);
                }
            }
        }
    }

    /**
     * 兼容方法：判断FTP服务器上文件是否存在（通用实现）
     *
     * @param ftpClient      FTP客户端
     * @param remoteFileName 远程文件名
     * @return true=存在，false=不存在
     */
    private boolean isFileExists(FTPClient ftpClient, String remoteFileName) {
        // 1. 前置参数校验：文件名不能为空
        if (remoteFileName == null || remoteFileName.trim().isEmpty()) {
            log.warn("远程文件名为空，无法判断是否存在");
            return false;
        }
        String fileName = remoteFileName.trim();

        // 2. 方案1：优先使用mlistFile（精准判断单个文件，性能最优）
        try {
            FTPFile ftpFile = ftpClient.mlistFile(fileName);
            if (ftpFile != null) {
                log.debug("【方案1-mlistFile】文件存在：{}", fileName);
                return true;
            }
            log.debug("【方案1-mlistFile】未检测到文件：{}", fileName);
        } catch (IOException e) {
            log.warn("【方案1-mlistFile】执行失败（服务器可能不支持MLST命令），文件名：{}，异常：{}",
                    fileName, e.getMessage());
        }

        // 3. 方案2：使用SIZE命令判断（轻量级，仅需获取文件大小，次优）
        try {
            long fileSize = ftpClient.size(fileName);
            boolean exists = fileSize >= 0;
            if (exists) {
                log.debug("【方案2-SIZE】文件存在：{}，文件大小：{}字节", fileName, fileSize);
            } else {
                log.debug("【方案2-SIZE】未检测到文件：{}", fileName);
            }
            return exists;
        } catch (IOException e) {
            log.warn("【方案2-SIZE】执行失败（服务器可能不支持SIZE命令），文件名：{}，异常：{}",
                    fileName, e.getMessage());
        }

        // 4. 方案3：兜底使用listFiles（遍历目录，性能最差，仅前序方案失败时执行）
        try {
            // listFiles支持文件名/路径模糊匹配，直接传入文件名即可精准匹配
            FTPFile[] files = ftpClient.listFiles(fileName);
            boolean exists = files != null && files.length > 0;
            if (exists) {
                log.debug("【方案3-listFiles】文件存在：{}，匹配到{}个文件", fileName, files.length);
            } else {
                log.debug("【方案3-listFiles】未检测到文件：{}", fileName);
            }
            return exists;
        } catch (IOException e) {
            log.error("【方案3-listFiles】执行失败（兜底方案），文件名：{}，异常：{}",
                    fileName, e.getMessage(), e);
        }

        // 5. 所有方案均失败，默认返回不存在（避免误判）
        log.warn("所有文件存在性判断方案均失败，默认返回文件不存在：{}", fileName);
        return false;
    }

}
