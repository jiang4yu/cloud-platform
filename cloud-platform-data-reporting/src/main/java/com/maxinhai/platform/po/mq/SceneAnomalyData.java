package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：SceneAnomalyData
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:26
 * @Description: 对应附录B.2.2 作业场景异常数据
 */
@Data
@Accessors(chain = true)
public class SceneAnomalyData extends BaseMessageEntity {

    // 消息体（必填）
    private String exceptionCode; // 14位异常编码（CJ+YYYYMMDD+4位顺序码，附录D.5）
    private String pointCode; // 14位点位编码
    private String eventCode; // 作业场景异常类型编码（B005-B012，附录D.9）
    private String eventDesc; // 异常场景的核心描述（必填，此前遗漏）
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
        // 补全PDF要求的EventDesc字段到消息体
        String body = String.format("%s;%s;%s;%s;%s;%s;%s;%s",
                exceptionCode, pointCode, eventCode, startTime,
                endTimeStr, eventDesc, eventImgUrl, dataGenerateTime);
        return header + "~" + body + "~||";
    }

}
