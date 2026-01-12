package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：PointCameraMappingInfo
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:23
 * @Description: 对应附录B.1.3 点位摄像仪关联信息
 */
@Data
@Accessors(chain = true)
public class PointCameraMappingInfo extends BaseMessageEntity {

    // 消息体（必填）
    private String pointCode; // 14位点位编码
    private String cameraStandardCode; // 20位摄像仪国标编码（GB/T28181-2022）
    private String cameraName; // 摄像仪名称（必填，此前遗漏）
    private String pointBaseImgUrl; // 非必填，点位基准图片URL
    private String dataGenerateTime; // 数据生成时间（14位）

    /**
     * 构建报文字符串
     */
    @Override
    public String buildMessage() {
        String header = String.format("%s;%s;%d;%s", getMineCode(), getMineName(), getMineType(), getUploadTime());
        String baseImgUrlStr = pointBaseImgUrl == null ? "" : pointBaseImgUrl;
        // 补全PDF要求的CameraName字段到消息体
        String body = String.format("%s;%s;%s;%s;%s",
                pointCode, cameraStandardCode, cameraName, baseImgUrlStr, dataGenerateTime);
        return header + "~" + body + "~||";
    }

}
