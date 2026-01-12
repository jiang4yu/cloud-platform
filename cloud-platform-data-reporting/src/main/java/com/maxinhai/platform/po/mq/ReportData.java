package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：ReportData
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:27
 * @Description: 对应附录B.3.2 报备数据
 */
@Data
@Accessors(chain = true)
public class ReportData extends BaseMessageEntity {

    // 消息体（必填）
    private String pointCode; // 14位点位编码
    private String reportStartTime; // 报备开始时间（14位）
    private String reportEndTime; // 报备结束时间（14位）
    private String reportReason; // 报备原因说明（不超过120字）
    private String dataGenerateTime; // 数据生成时间（14位）

    /**
     * 构建报文字符串
     */
    @Override
    public String buildMessage() {
        String header = String.format("%s;%s;%d;%s", getMineCode(), getMineName(), getMineType(), getUploadTime());
        String body = String.format("%s;%s;%s;%s;%s",
                pointCode, reportStartTime, reportEndTime, reportReason, dataGenerateTime);
        return header + "~" + body + "~||";
    }

}
