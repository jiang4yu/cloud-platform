package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：PointBasicInfo
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:23
 * @Description: 对应附录B.1.2 点位基础信息
 */
@Data
@Accessors(chain = true)
public class PointBasicInfo extends BaseMessageEntity {

    // 消息体（必填）
    private String pointCode; // 14位点位编码（附录D.4：DW+安装位置编码+3位顺序码）
    private String pointName; // 点位名称（参考附录A.1/A.3命名规范）
    private int pointStatus; // 0=移除/1=使用
    private String pointAbility; // 点位能力（多个用&拼接，如"A001&A002"）
    private String creationTime; // 点位创建时间（14位）
    private String removeTime; // 非必填，点位移除时填写（14位）
    private int confirmStatus; // 0=未确认/1=确认
    private String confirmPerson; // 非必填，确认人姓名
    private String confirmTime; // 非必填，确认时间（14位）
    private String positioningSubstationCode; // 非必填，人员定位分站编码
    private String positioningSubstationName; // 非必填，人员定位分站名称
    private String dataGenerateTime; // 数据生成时间（14位）

    /**
     * 构建报文字符串
     */
    @Override
    public String buildMessage() {
        String header = String.format("%s;%s;%d;%s", getMineCode(), getMineName(), getMineType(), getUploadTime());
        String removeTimeStr = removeTime == null ? "" : removeTime;
        String confirmPersonStr = confirmPerson == null ? "" : confirmPerson;
        String confirmTimeStr = confirmTime == null ? "" : confirmTime;
        String substationCodeStr = positioningSubstationCode == null ? "" : positioningSubstationCode;
        String substationNameStr = positioningSubstationName == null ? "" : positioningSubstationName;

        String body = String.format("%s;%s;%d;%s;%s;%s;%d;%s;%s;%s;%s;%s",
                pointCode, pointName, pointStatus, pointAbility, creationTime,
                removeTimeStr, confirmStatus, confirmPersonStr, confirmTimeStr,
                substationCodeStr, substationNameStr, dataGenerateTime);
        return header + "~" + body + "~||";
    }

}
