package com.maxinhai.platform.po.ftp;

import lombok.Data;

/**
 * @ClassName：InstallPositionData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:12
 * @Description: 安装位置基础信息实体（对应附录B.1.1）
 */
@Data
public class InstallPositionData {

    // 区域名称（必填）
    private String areaName;
    // 区域编码（必填，5位）
    private String areaCode;
    // 区域状态（必填，0=停用，1=使用）
    private Integer areaStatus;
    // 安装位置分类编号（必填，参考附录A）
    private String positionClassCode;
    // 是否涉及（必填，0=不涉及，1=涉及）
    private Integer isInvolved;
    // 不涉及说明（非必填）
    private String notInvolvedDesc;
    // 安装位置名称（必填）
    private String positionName;
    // 安装位置编码（必填，9位）
    private String positionCode;
    // 安装位置状态（必填，0=停用，1=使用）
    private Integer positionStatus;
    // 数据生成时间（必填，yyyyMMddHHmmss）
    private String dataTime;

}
