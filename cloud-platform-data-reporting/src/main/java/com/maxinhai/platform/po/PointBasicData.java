package com.maxinhai.platform.po;

import lombok.Data;

import java.util.List;

/**
 * @ClassName：PointBasicData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:27
 * @Description: 点位基础信息（附录B.1.2）
 */
@Data
public class PointBasicData {

    // 点位编码（必填，14位）
    private String pointCode;
    // 点位名称（必填）
    private String pointName;
    // 点位状态（必填，0=移除，1=使用）
    private Integer pointStatus;
    // 点位能力（必填，多值用&分隔）
    private List<String> pointAbilities;
    // 点位创建时间（必填，yyyyMMddHHmmss）
    private String creationTime;
    // 点位移除时间（非必填）
    private String removeTime = "";
    // 点位确认状态（必填，0=未确认，1=确认）
    private Integer confirmStatus;
    // 确认人（非必填）
    private String confirmPerson = "";
    // 确认时间（非必填）
    private String confirmTime = "";
    // 人员定位分站编码（非必填）
    private String positioningSubstationCode = "";
    // 人员定位分站名称（非必填）
    private String positioningSubstationName = "";
    // 数据生成时间（必填）
    private String dataTime;

}
