package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.DrillDesignData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：DrillDesignParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:37
 * @Description: 重大灾害钻孔施工设计信息解析器（附录C.5）
 */
@Component
public class DrillDesignParser implements DataParser<DrillDesignData> {

    @Override
    public String parse(DrillDesignData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getDrillCode(),
                data.getDrillType(),
                data.getDesignDepth(),
                data.getConstructionUnit(),
                data.getDataTime()
        );
    }

}
