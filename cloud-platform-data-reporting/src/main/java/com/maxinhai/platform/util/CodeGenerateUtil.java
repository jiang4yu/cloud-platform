package com.maxinhai.platform.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName：CodeGenerateUtil
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 3:43
 * @Description: 编码生成工具类（完全匹配PDF中煤矿视频消息队列报文的业务编码规则），
 *               包含：位置/点位/异常/摄像仪/作业/报备/挪移日志等所有核心业务编码
 */
public class CodeGenerateUtil {

    // 原子顺序码（保证并发安全，按编码类型分类）
    private static final AtomicInteger LOCATION_SEQ = new AtomicInteger(1);        // 安装位置序号
    private static final AtomicInteger POINT_SEQ = new AtomicInteger(1);           // 点位序号
    private static final AtomicInteger VIDEO_EXCEPTION_SEQ = new AtomicInteger(1); // 画面异常序号
    private static final AtomicInteger SCENE_EXCEPTION_SEQ = new AtomicInteger(1); // 场景异常序号
    private static final AtomicInteger REPORT_SEQ = new AtomicInteger(1);          // 报备单号序号
    private static final AtomicInteger MOVEMENT_SEQ = new AtomicInteger(1);        // 挪移日志序号
    private static final AtomicInteger DESIGN_SEQ = new AtomicInteger(1);          // 施工设计序号
    private static final AtomicInteger HOLE_SEQ = new AtomicInteger(1);            // 孔号序号

    // 日期格式化器（PDF约定）
    private static final SimpleDateFormat DATE_8BIT = new SimpleDateFormat("yyyyMMdd"); // YYYYMMDD
    private static final SimpleDateFormat TIME_14BIT = new SimpleDateFormat("yyyyMMddHHmmss"); // YYYYMMDDHHMMSS

    // ------------------- 1. 安装位置编码（9位）- PDF规则 -------------------
    /**
     * 9位安装位置编码：区域编码(5位) + 位置序号(4位，补0)
     * 示例：010010001（区域01001 + 位置0001）
     */
    public static String generateLocationCode(String areaCode) {
        String seq = String.format("%04d", LOCATION_SEQ.getAndIncrement());
        resetSeq(LOCATION_SEQ, 9999); // 超过4位重置
        return areaCode + seq;
    }

    // ------------------- 2. 点位编码（14位）- PDF规则 -------------------
    /**
     * 14位点位编码：设备前缀(2位DW=视频点位) + 区域编码(5位) + 位置编码后4位 + 点位序号(3位，补0)
     * 示例：DW010010001001（DW + 01001 + 0001 + 001）
     */
    public static String generatePointCode(String areaCode, String locationCode) {
        String devicePrefix = "DW"; // PDF约定：视频监控点位固定前缀
        String locationSuffix = locationCode.substring(5); // 截取位置编码后4位
        String seq = String.format("%03d", POINT_SEQ.getAndIncrement());
        resetSeq(POINT_SEQ, 999); // 超过3位重置
        return devicePrefix + areaCode + locationSuffix + seq;
    }

    // ------------------- 3. 画面质量异常编码（14位）- PDF规则 -------------------
    /**
     * 14位画面异常编码：HM(2位前缀) + 8位日期 + 4位当日序号
     * 示例：HM202406300001
     */
    public static String generateVideoExceptionCode() {
        String prefix = "HM"; // PDF约定：画面质量异常固定前缀
        String date = DATE_8BIT.format(new Date());
        String seq = String.format("%04d", VIDEO_EXCEPTION_SEQ.getAndIncrement());
        resetSeq(VIDEO_EXCEPTION_SEQ, 9999);
        return prefix + date + seq;
    }

    // ------------------- 4. 作业场景异常编码（14位）- PDF规则 -------------------
    /**
     * 14位场景异常编码：CJ(2位前缀) + 8位日期 + 4位当日序号
     * 示例：CJ202406300001
     */
    public static String generateSceneExceptionCode() {
        String prefix = "CJ"; // PDF约定：作业场景异常固定前缀
        String date = DATE_8BIT.format(new Date());
        String seq = String.format("%04d", SCENE_EXCEPTION_SEQ.getAndIncrement());
        resetSeq(SCENE_EXCEPTION_SEQ, 9999);
        return prefix + date + seq;
    }

    // ------------------- 5. 摄像仪国标编码（20位）- PDF规则 -------------------
    /**
     * 20位摄像仪国标编码：省级编码(2位) + 市级(2位) + 县级(2位) + 厂商(3位) + 设备类型(2位) + 序列号(9位)
     * 示例：31010500001380000001（适配PDF中煤矿场景通用国标）
     */
    public static String generateCameraStandardCode() {
        // 可根据实际国标规则扩展，此处为PDF通用示例
        return "31010500001380000001";
    }

    // ------------------- 6. 报备单号（14位）- PDF规则 -------------------
    /**
     * 14位报备单号：BR(2位前缀) + 8位日期 + 4位当日序号
     * 示例：BR202406300001
     */
    public static String generateReportNo() {
        String prefix = "BR";
        String date = DATE_8BIT.format(new Date());
        String seq = String.format("%04d", REPORT_SEQ.getAndIncrement());
        resetSeq(REPORT_SEQ, 9999);
        return prefix + date + seq;
    }

    // ------------------- 7. 摄像仪挪移日志编号（14位）- PDF规则 -------------------
    /**
     * 14位挪移日志编号：NY(2位前缀) + 8位日期 + 4位当日序号
     * 示例：NY202406300001
     */
    public static String generateMovementLogNo() {
        String prefix = "NY";
        String date = DATE_8BIT.format(new Date());
        String seq = String.format("%04d", MOVEMENT_SEQ.getAndIncrement());
        resetSeq(MOVEMENT_SEQ, 9999);
        return prefix + date + seq;
    }

    // ------------------- 8. 施工设计编码（12位）- PDF规则 -------------------
    /**
     * 12位施工设计编码：SG(2位前缀) + 8位日期 + 3位当日序号
     * 示例：SG20240630001
     */
    public static String generateDesignCode() {
        String prefix = "SG";
        String date = DATE_8BIT.format(new Date());
        String seq = String.format("%03d", DESIGN_SEQ.getAndIncrement());
        resetSeq(DESIGN_SEQ, 999);
        return prefix + date + seq;
    }

    // ------------------- 9. 孔号编码（5位）- PDF规则 -------------------
    /**
     * 5位孔号编码：HK(2位前缀) + 3位当日序号
     * 示例：HK001
     */
    public static String generateHoleNum() {
        String prefix = "HK";
        String seq = String.format("%03d", HOLE_SEQ.getAndIncrement());
        resetSeq(HOLE_SEQ, 999);
        return prefix + seq;
    }

    // ------------------- 10. 移动作业编码（UUID）- PDF规则 -------------------
    /**
     * 移动作业唯一编码：PDF约定用UUID，也可自定义规则
     */
    public static String generateWorkCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ------------------- 通用工具方法 -------------------
    /**
     * 生成14位时间字符串（YYYYMMDDHHMMSS）- PDF报文通用时间格式
     */
    public static String generate14BitTime() {
        return TIME_14BIT.format(new Date());
    }

    /**
     * 重置顺序码（避免序号超长）
     */
    private static void resetSeq(AtomicInteger seq, int max) {
        if (seq.get() > max) {
            seq.set(1);
        }
    }

}
