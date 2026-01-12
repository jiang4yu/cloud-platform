package com.maxinhai.platform.po.mq;

import lombok.Data;

/**
 * @ClassName：BaseMessageEntity
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:50
 * @Description: 统一报文实体父类（规范报文构造）
 */
@Data
public abstract class BaseMessageEntity {

    // 消息头通用字段（所有数据类型共用）
    private String mineCode; // 12位煤矿编码
    private String mineName; // 煤矿名称
    private int mineType; // 1=井工矿/2=露天矿
    private String uploadTime; // 数据上传时间（14位，通过DateUtil生成）

    // 抽象方法：子类实现，构造对应数据类型的报文字符串
    public abstract String buildMessage();

}
