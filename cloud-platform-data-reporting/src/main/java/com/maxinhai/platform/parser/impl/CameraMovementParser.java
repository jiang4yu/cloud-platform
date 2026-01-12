package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.CameraMovementData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：CameraMovementParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:37
 * @Description: 摄像仪挪移日志解析器（附录C.3）
 */
@Component
public class CameraMovementParser implements DataParser<CameraMovementData> {

    @Override
    public String parse(CameraMovementData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getCameraSn(),
                data.getOldPointCode(),
                data.getNewPointCode(),
                data.getMoveTime(),
                data.getOperator(),
                data.getDataTime()
        );
    }

}
