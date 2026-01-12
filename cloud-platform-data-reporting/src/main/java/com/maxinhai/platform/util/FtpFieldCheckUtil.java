package com.maxinhai.platform.util;

import com.maxinhai.platform.constant.FtpConstant;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName：FtpFieldCheckUtil
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:47
 * @Description: 字段校验工具类
 */
@Slf4j
public class FtpFieldCheckUtil {

    /**
     * 校验煤矿编码（12位）
     */
    public static boolean checkCoalMineCode(String code) {
        return code != null && code.length() == FtpConstant.COAL_MINE_CODE_LENGTH && code.matches("^[0-9A-Za-z]+$");
    }

    /**
     * 获取字符集（默认UTF-8）
     */
    public static Charset getCharset(String charset) {
        try {
            return Charset.forName(charset);
        } catch (Exception e) {
            log.warn("字符集{}不支持，使用默认UTF-8", charset);
            return StandardCharsets.UTF_8;
        }
    }

    /**
     * 校验日期格式（yyyyMMddHHmmss）
     */
    public static boolean checkDateFormat(String dateStr) {
        if (dateStr == null || dateStr.length() != 14) {
            return false;
        }
        return dateStr.matches("^\\d{14}$");
    }

}
