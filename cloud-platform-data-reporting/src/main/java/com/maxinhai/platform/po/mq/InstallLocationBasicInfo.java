package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：InstallLocationBasicInfo
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:13
 * @Description: 安装位置基础信息（对应附录B.1.1）
 */
@Data
@Accessors(chain = true)
public class InstallLocationBasicInfo extends BaseMessageEntity {

    // 消息体字段
    private String areaName; // 区域名称
    private String areaCode; // 区域编码（5位）
    private int areaStatus; // 区域状态（0=停用/1=使用）
    private String locationClassCode; // 安装位置分类编号
    private int isInvolved; // 是否涉及（0=不涉及/1=涉及）
    private String notInvolvedDesc; // 不涉及说明（非必填）
    private String locationName; // 安装位置名称
    private String locationCode; // 安装位置编码（9位）
    private int locationStatus; // 安装位置状态（0=停用/1=使用）
    private String dataGenerateTime; // 数据生成时间（14位）

    /**
     * 构造报文字符串
     * 格式：消息头~消息体~||
     */
    @Override
    public String buildMessage() {
        // 消息头
        String header = String.format("%s;%s;%d;%s",
                getMineCode(), getMineName(), getMineType(), getUploadTime());
        // 消息体
        String body = String.format("%s;%s;%d;%s;%d;%s;%s;%s;%d;%s",
                areaName, areaCode, areaStatus, locationClassCode,
                isInvolved, notInvolvedDesc == null ? "" : notInvolvedDesc,
                locationName, locationCode, locationStatus, dataGenerateTime);
        // 完整报文
        return header + "~" + body + "~||";
    }

}
