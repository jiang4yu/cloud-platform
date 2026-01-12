package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.ftp.InstallPositionData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：InstallPositionParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:34
 * @Description: 安装位置基础信息解析器
 */
@Component
public class InstallPositionParser implements DataParser<InstallPositionData> {

    @Override
    public String parse(InstallPositionData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getAreaName(),
                data.getAreaCode(),
                data.getAreaStatus().toString(),
                data.getPositionClassCode(),
                data.getIsInvolved().toString(),
                data.getNotInvolvedDesc(),
                data.getPositionName(),
                data.getPositionCode(),
                data.getPositionStatus().toString(),
                data.getDataTime()
        );
    }

}
