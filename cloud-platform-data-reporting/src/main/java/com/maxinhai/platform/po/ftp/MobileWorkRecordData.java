package com.maxinhai.platform.po.ftp;

import lombok.Data;

/**
 * @ClassName：MobileWorkRecordData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:32
 * @Description: 移动作业记录
 */
@Data
public class MobileWorkRecordData {

    private String workCode;        // 作业编码（必填）
    private String workType;        // 作业类型（必填，附录C.4）
    private String workStartTime;   // 作业开始时间（必填）
    private String workEndTime;     // 作业结束时间（必填）
    private String workResult;      // 作业结果（必填，0=失败，1=成功）
    private String dataTime;        // 数据生成时间（必填）

}
