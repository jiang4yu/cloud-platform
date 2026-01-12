package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.PointBasicData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName：PointBasicDataParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:21
 * @Description: 点位基础信息解析器（对应规范B.1.2）
 */
@Component
public class PointBasicParser implements DataParser<PointBasicData> {

    @Override
    public String parse(PointBasicData data) {
        // 多值拼接（点位能力）
        List<String> abilities = data.getPointAbilities();
        String abilityStr = abilities == null ? "" : String.join(FtpConstant.MULTI_VALUE_SEPARATOR, abilities);

        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getPointCode(),
                data.getPointName(),
                data.getPointStatus().toString(),
                abilityStr,
                data.getCreationTime(),
                data.getRemoveTime(),
                data.getConfirmStatus().toString(),
                data.getConfirmPerson(),
                data.getConfirmTime(),
                data.getPositioningSubstationCode(),
                data.getPositioningSubstationName(),
                data.getDataTime()
        );
    }
}
