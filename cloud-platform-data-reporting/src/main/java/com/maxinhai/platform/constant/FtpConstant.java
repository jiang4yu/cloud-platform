package com.maxinhai.platform.constant;

/**
 * @ClassName：FtpConstant
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:26
 * @Description: FTP报文通用常量（严格遵循细则要求）
 */
public class FtpConstant {

    // 字段分隔符（英文半角分号）
    public static final String FIELD_SEPARATOR = ";";
    // 记录结束符
    public static final String RECORD_END = "~";
    // 数据组结束符
    public static final String GROUP_END = "||";
    // 多值分隔符（如点位能力）
    public static final String MULTI_VALUE_SEPARATOR = "&";
    // 文件后缀（固定TXT）
    public static final String FILE_SUFFIX = ".txt";
    // 煤矿编码长度（12位）
    public static final int COAL_MINE_CODE_LENGTH = 12;
    // 随机数位数（4位）
    public static final int RANDOM_NUM_LENGTH = 4;
    // 日期格式（yyyyMMddHHmmss）
    public static final String DATE_FORMAT = "yyyyMMddHHmmss";

}
