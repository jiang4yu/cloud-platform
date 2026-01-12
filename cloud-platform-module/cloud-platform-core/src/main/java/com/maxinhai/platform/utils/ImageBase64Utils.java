package com.maxinhai.platform.utils;

import cn.hutool.core.date.DateUtil;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

/**
 * @ClassName：ImageBase64Utils
 * @Author: XinHai.Ma
 * @Date: 2025/12/2 18:25
 * @Description: 图片 Base64 转换工具类
 */
public class ImageBase64Utils {

    /**
     * 图片文件转为Base64编码
     *
     * @param imagePath 图片绝对路径（如："C:/test/1.png" 或 "/Users/test/1.jpg"）
     * @return Base64编码字符串（带格式前缀，如 "data:image/png;base64,xxx"）
     * @throws IOException 图片读取失败（文件不存在、权限不足等）
     */
    public static String imageToBase64(String imagePath) throws IOException {
        // 1. 验证文件是否存在
        File imageFile = new File(imagePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            throw new FileNotFoundException("图片文件不存在：" + imagePath);
        }

        // 2. 读取图片字节数组
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));

        // 3. Base64编码
        String base64Str = Base64.encodeBase64String(imageBytes);

        // 4. 添加格式前缀（自动识别图片类型）
        String suffix = getImageSuffix(imagePath);
        String contentType = getContentTypeBySuffix(suffix);
        return "data:" + contentType + ";base64," + base64Str;
    }

    /**
     * Base64编码转为图片并保存到本地
     *
     * @param base64Str Base64编码字符串（支持带前缀或不带前缀，如 "data:image/png;base64,xxx" 或 "xxx"）
     * @param saveDir   保存目录（如："C:/test/output" 或 "/Users/test/output"）
     * @param fileName  保存文件名（需带后缀，如 "result.png"、"output.jpg"）
     * @throws IOException 图片写入失败（目录不存在、权限不足等）
     */
    public static void base64ToImage(String base64Str, String saveDir, String fileName) throws IOException {
        // 1. 去除Base64前缀（兼容带前缀和不带前缀的情况）
        String pureBase64 = base64Str;
        if (base64Str.startsWith("data:image/")) {
            pureBase64 = base64Str.split(";base64,")[1];
        }

        // 2. Base64解码为字节数组
        byte[] imageBytes = Base64.decodeBase64(pureBase64);

        // 3. 处理保存目录（自动创建不存在的目录，跨平台兼容）
        File dir = new File(saveDir);
        if (!dir.exists()) {
            boolean mkdirSuccess = dir.mkdirs(); // 递归创建多级目录
            if (!mkdirSuccess) {
                throw new IOException("创建保存目录失败：" + saveDir);
            }
        }

        // 4. 拼接完整保存路径（用File.separator适配不同系统的路径分隔符）
        String savePath = saveDir + File.separator + fileName;

        // 5. 写入文件（使用缓冲流提升性能）
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(savePath))) {
            outputStream.write(imageBytes);
        }
        System.out.println("图片保存成功：" + savePath);
    }

    /**
     * 从文件路径获取图片后缀（如 "png"、"jpg"）
     */
    private static String getImageSuffix(String imagePath) {
        return imagePath.substring(imagePath.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 根据后缀获取Content-Type（如 "image/png"、"image/jpeg"）
     */
    private static String getContentTypeBySuffix(String suffix) {
        switch (suffix) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            default:
                return "image/jpeg"; // 默认类型
        }
    }

    // ------------------------------ 测试方法 ------------------------------
    public static void main(String[] args) {
        try {
            // 1. 测试：图片转Base64（替换为你的图片路径）
            String imagePath = "C:/Users/maxin/Pictures/aoteman001.jpg"; // Windows示例
            // String imagePath = "/Users/test/input.jpg"; // Mac/Linux示例
            String base64 = imageToBase64(imagePath);
            System.out.println("图片转Base64成功，编码长度：" + base64.length());

            // 2. 测试：Base64转图片并保存（替换为你的保存路径）
            String saveDir = "C:/Coalbot/ISAPI/image"; // Windows示例
            // String saveDir = "/Users/test/output"; // Mac/Linux示例
            String fileName = DateUtil.format(new Date(), "yyyy_MM_dd_HH_mm_ss") + ".jpg";
            base64ToImage(base64, saveDir, fileName);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("操作失败：" + e.getMessage());
        }
    }

}
