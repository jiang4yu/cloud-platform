package com.maxinhai.platform.po.ftp;

import lombok.Data;

/**
 * @ClassName：PointCameraMappingData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:28
 * @Description: 点位摄像仪关联信息（附录B.1.3）
 */
@Data
public class PointCameraMappingData {

    // 点位编码（必填，14位）
    private String pointCode;
    // 摄像仪厂商（必填）
    private String cameraVendor;
    // 摄像仪型号（必填）
    private String cameraModel;
    // 摄像仪序列号（必填，唯一）
    private String cameraSn;
    // 摄像仪IP地址（必填）
    private String cameraIp;
    // 摄像仪端口（必填）
    private Integer cameraPort;
    // 接入平台编码（必填）
    private String platformCode;
    // 关联状态（必填，0=解除，1=关联）
    private Integer mappingStatus;
    // 关联时间（必填，yyyyMMddHHmmss）
    private String mappingTime;
    // 解除时间（非必填）
    private String unMappingTime = "";
    // 数据生成时间（必填）
    private String dataTime;

}
