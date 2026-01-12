package com.maxinhai.platform.po;

import lombok.Data;

/**
 * @ClassName：CameraRealTimeData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:29
 * @Description: 摄像仪实时数据
 */
@Data
public class CameraRealTimeData {

    private String cameraSn;        // 摄像仪序列号（必填）
    private String onlineStatus;    // 在线状态（必填，0=离线，1=在线）
    private String videoQuality;    // 视频质量（必填，0=差，1=中，2=优）
    private String storageStatus;   // 存储状态（必填，0=异常，1=正常）
    private String dataTime;        // 数据生成时间（必填）

}
