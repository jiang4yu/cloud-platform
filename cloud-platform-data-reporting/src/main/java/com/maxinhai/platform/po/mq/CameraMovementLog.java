package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：CameraMovementLog
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:27
 * @Description: 对应附录B.3.1 点位关联摄像仪挪移日志
 */
@Data
@Accessors(chain = true)
public class CameraMovementLog extends BaseMessageEntity {

    // 消息体（必填）
    private int changeType; // 0=解绑/1=绑定
    private String pointCode; // 14位点位编码
    private String cameraStandardCode; // 20位摄像仪国标编码
    private String movementReason; // 非必填，解绑时必填（如"搬家倒面"）
    private String movementTime; // 挪移时间（14位）
    private String dataGenerateTime; // 数据生成时间（14位）

    /**
     * 构建报文字符串
     */
    @Override
    public String buildMessage() {
        String header = String.format("%s;%s;%d;%s", getMineCode(), getMineName(), getMineType(), getUploadTime());
        String reasonStr = movementReason == null ? "" : movementReason;
        String body = String.format("%d;%s;%s;%s;%s;%s",
                changeType, pointCode, cameraStandardCode, reasonStr,
                movementTime, dataGenerateTime);
        return header + "~" + body + "~||";
    }

}
