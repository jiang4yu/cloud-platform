package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：VideoQualityAnomalyData
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:25
 * @Description: 对应附录B.2.1 画面质量异常数据
 */
@Data
@Accessors(chain = true)
public class VideoQualityAnomalyData extends BaseMessageEntity {

    // 消息体（必填）
    private String exceptionCode; // 14位异常编码（HM+YYYYMMDD+4位顺序码，附录D.5）
    private String pointCode; // 14位点位编码
    private String baseImgUrl; // 点位基准图片URL
    private String eventCode; // 画面质量异常类型编码（A001-A008，附录D.7）
    private String startTime; // 异常开始时间（14位）
    private String endTime; // 非必填，异常结束时填写（14位）
    private String eventImgUrl; // 异常图片URL
    private String dataGenerateTime; // 数据生成时间（14位）

    /**
     * 构建报文字符串
     */
    @Override
    public String buildMessage() {
        String header = String.format("%s;%s;%d;%s", getMineCode(), getMineName(), getMineType(), getUploadTime());
        String endTimeStr = endTime == null ? "" : endTime;
        String body = String.format("%s;%s;%s;%s;%s;%s;%s;%s",
                exceptionCode, pointCode, baseImgUrl, eventCode,
                startTime, endTimeStr, eventImgUrl, dataGenerateTime);
        return header + "~" + body + "~||";
    }

}
