package com.maxinhai.platform.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.util.Date;

/**
 * @ClassName：ImageBase64HutoolUtils
 * @Author: XinHai.Ma
 * @Date: 2025/12/2 18:31
 * @Description: 基于Hutool的图片Base64转换工具类（跨平台兼容）
 * 依赖：Hutool核心包（无需额外依赖其他工具）
 */
public class ImageBase64HutoolUtils {

    /**
     * 图片文件转为Base64编码（自动识别格式，带标准前缀）
     *
     * @param imagePath 图片绝对路径（如 "C:/test/1.png" 或 "/Users/test/1.jpg"）
     * @return Base64编码字符串（格式：data:image/xxx;base64,xxx）
     */
    public static String imageToBase64(String imagePath) {
        // 1. Hutool FileUtil验证文件并读取字节（自动处理异常）
        File imageFile = FileUtil.file(imagePath);
        if (!FileUtil.exist(imageFile) || !FileUtil.isFile(imageFile)) {
            throw new IllegalArgumentException("图片文件不存在或不是文件：" + imagePath);
        }
        byte[] imageBytes = FileUtil.readBytes(imageFile);

        // 2. 自动识别图片格式，生成带前缀的Base64（Hutool核心简化）
        String suffix = FileUtil.extName(imagePath).toLowerCase();
        String contentType = getContentTypeBySuffix(suffix);
        return StrUtil.format("data:{};base64,{}", contentType, Base64.encode(imageBytes));
    }

    /**
     * Base64编码转为图片并保存（兼容带/不带前缀，自动创建目录）
     *
     * @param base64Str Base64字符串（支持两种格式：带前缀/不带前缀）
     * @param saveDir   保存目录（如 "C:/test/output" 或 "/Users/test/output"）
     * @param fileName  保存文件名（需带后缀，如 "result.png"）
     */
    public static void base64ToImage(String base64Str, String saveDir, String fileName) {
        // 1. 去除Base64前缀（Hutool StrUtil简化字符串处理）
        String pureBase64 = StrUtil.startWith(base64Str, "data:image/")
                ? StrUtil.split(base64Str, ";base64,").get(1)
                : base64Str;

        // 2. Base64解码（Hutool Base64工具自动处理）
        byte[] imageBytes = Base64.decode(pureBase64);

        // 3. 跨平台路径拼接 + 自动创建目录（Hutool核心简化）
        File saveFile = FileUtil.file(saveDir, fileName);
        FileUtil.mkParentDirs(saveFile); // 递归创建父目录（跨平台兼容）

        // 4. 写入文件（Hutool FileUtil简化IO操作）
        FileUtil.writeBytes(imageBytes, saveFile);
        System.out.println("图片保存成功：" + saveFile.getAbsolutePath());
    }

    /**
     * 辅助方法：根据后缀获取Content-Type（复用核心逻辑）
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
            // 1. 图片转Base64（替换为你的图片路径）
            String imagePath = "C:/Users/maxin/Pictures/telangpu.png"; // Windows示例
            // String imagePath = "/Users/test/input.jpg"; // Mac/Linux示例
            String base64 = imageToBase64(imagePath);
            System.out.println("Base64编码长度：" + base64.length());

            // 2. Base64转图片保存（替换为你的保存路径）
            String saveDir = "C:/Coalbot/ISAPI/image"; // Windows示例
            // String saveDir = "/Users/test/output"; // Mac/Linux示例
            base64ToImage(base64, saveDir, DateUtil.format(new Date(), "yyyy_MM_dd_HH_mm_ss") + ".png");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("操作失败：" + e.getMessage());
        }
    }

}
