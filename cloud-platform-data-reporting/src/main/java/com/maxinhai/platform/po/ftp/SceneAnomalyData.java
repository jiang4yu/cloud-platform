package com.maxinhai.platform.po.ftp;

import lombok.Data;

/**
 * @ClassName：SceneAnomalyData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:30
 * @Description: 作业场景异常数据
 */
@Data
public class SceneAnomalyData {

    private String cameraSn;        // 摄像仪序列号（必填）
    private String anomalyType;     // 异常类型（必填，附录C.2）
    private String anomalyTime;     // 异常时间（必填）
    private String handleResult;    // 处理结果（非必填）
    private String dataTime;        // 数据生成时间（必填）

}
