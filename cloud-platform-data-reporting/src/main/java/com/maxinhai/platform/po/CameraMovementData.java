package com.maxinhai.platform.po;

import lombok.Data;

/**
 * @ClassName：CameraMovementData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:31
 * @Description: 摄像仪挪移日志
 */
@Data
public class CameraMovementData {

    private String cameraSn;        // 摄像仪序列号（必填）
    private String oldPointCode;    // 原点位编码（必填）
    private String newPointCode;    // 新点位编码（必填）
    private String moveTime;        // 挪移时间（必填）
    private String operator;        // 操作人（必填）
    private String dataTime;        // 数据生成时间（必填）

}
