package com.maxinhai.platform.po;

import lombok.Data;

/**
 * @ClassName：ReportData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:31
 * @Description: 报备数据
 */
@Data
public class ReportData {

    private String reportType;      // 报备类型（必填，0=安装，1=挪移，2=拆除）
    private String reportTime;      // 报备时间（必填）
    private String approveStatus;   // 审批状态（必填，0=未审批，1=通过，2=驳回）
    private String approveTime;     // 审批时间（非必填）
    private String dataTime;        // 数据生成时间（必填）

}
