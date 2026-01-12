package com.maxinhai.platform.po.ftp;

import lombok.Data;

/**
 * @ClassName：RecordStorageCheckData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:29
 * @Description: 录像存储状况巡检
 */
@Data
public class RecordStorageCheckData {

    private String cameraSn;        // 摄像仪序列号（必填）
    private String checkTime;       // 巡检时间（必填）
    private String storageDays;     // 存储天数（必填，整数）
    private String checkResult;     // 巡检结果（必填，0=异常，1=正常）
    private String exceptionDesc;   // 异常说明（非必填）
    private String dataTime;        // 数据生成时间（必填）

}
