package com.maxinhai.platform.po.ftp;

import lombok.Data;

/**
 * @ClassName：DrillDesignData
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:32
 * @Description: 重大灾害钻孔施工设计
 */
@Data
public class DrillDesignData {

    private String drillCode;       // 钻孔编码（必填）
    private String drillType;       // 钻孔类型（必填，附录C.3）
    private String designDepth;     // 设计深度（必填，数值）
    private String constructionUnit;// 施工单位（必填）
    private String dataTime;        // 数据生成时间（必填）

}
