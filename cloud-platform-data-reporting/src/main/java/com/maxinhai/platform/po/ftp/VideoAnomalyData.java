package com.maxinhai.platform.po.ftp;

import lombok.Data;

/**
 * @ClassName：VideoAnomalyData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:30
 * @Description: 画面质量异常数据
 */
@Data
public class VideoAnomalyData {

    private String cameraSn;        // 摄像仪序列号（必填）
    private String anomalyType;     // 异常类型（必填，0=花屏，1=黑屏，2=卡顿）
    private String anomalyTime;     // 异常时间（必填）
    private String recoverTime;     // 恢复时间（非必填）
    private String dataTime;        // 数据生成时间（必填）

}
