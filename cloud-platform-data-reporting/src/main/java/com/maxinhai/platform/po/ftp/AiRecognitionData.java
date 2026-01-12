package com.maxinhai.platform.po.ftp;

import lombok.Data;

/**
 * @ClassName：AiRecognitionData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:29
 * @Description: AI作业场景识别数据
 */
@Data
public class AiRecognitionData {

    private String cameraSn;        // 摄像仪序列号（必填）
    private String sceneType;       // 场景类型（必填，附录C.1）
    private String recognitionTime; // 识别时间（必填）
    private String recognitionResult;// 识别结果（必填，0=无，1=有）
    private String picUrl;          // 截图URL（非必填）
    private String dataTime;        // 数据生成时间（必填）

}
