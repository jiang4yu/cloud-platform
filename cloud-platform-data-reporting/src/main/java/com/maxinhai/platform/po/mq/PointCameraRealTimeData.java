package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：PointCameraRealTimeData
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:24
 * @Description: 对应附录B.1.3.10 点位摄像仪实时数据
 */
@Data
@Accessors(chain = true)
public class PointCameraRealTimeData extends BaseMessageEntity {

    // 消息体（必填）
    private String pointCode; // 14位点位编码
    private String cameraStandardCode; // 20位摄像仪国标编码（必填，此前遗漏）
    private int onlineStatus; // 0=离线/1=在线
    private String dataGenerateTime; // 数据生成时间（14位）

    /**
     * 构建报文字符串
     */
    @Override
    public String buildMessage() {
        String header = String.format("%s;%s;%d;%s", getMineCode(), getMineName(), getMineType(), getUploadTime());
        // 补全PDF要求的CameraStandardCode字段到消息体
        String body = String.format("%s;%s;%d;%s", pointCode, cameraStandardCode, onlineStatus, dataGenerateTime);
        return header + "~" + body + "~||";
    }

}
