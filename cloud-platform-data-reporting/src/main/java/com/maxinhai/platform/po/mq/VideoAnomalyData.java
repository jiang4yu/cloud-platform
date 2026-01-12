package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：VideoAnomalyData
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:14
 * @Description: 画面质量异常数据（对应附录B.2.1）
 */
@Data
@Accessors(chain = true)
public class VideoAnomalyData extends BaseMessageEntity {

    // 消息体字段
    private String exceptionCode; // 异常编码（14位）
    private String pointCode; // 点位编码（14位）
    private String baseImgUrl; // 点位基准图片地址
    private String eventCode; // 异常事件类型编码（A001-A008）
    private String startTime; // 异常开始时间（14位）
    private String endTime; // 异常结束时间（14位，非必填）
    private String eventImgUrl; // 异常图片地址
    private String dataGenerateTime; // 数据生成时间（14位）

    /**
     * 构造报文字符串
     */
    @Override
    public String buildMessage() {
        String header = String.format("%s;%s;%d;%s",
                getMineCode(), getMineName(), getMineType(), getUploadTime());
        String body = String.format("%s;%s;%s;%s;%s;%s;%s;%s",
                exceptionCode, pointCode, baseImgUrl, eventCode,
                startTime, endTime == null ? "" : endTime,
                eventImgUrl, dataGenerateTime);
        return header + "~" + body + "~||";
    }

}
