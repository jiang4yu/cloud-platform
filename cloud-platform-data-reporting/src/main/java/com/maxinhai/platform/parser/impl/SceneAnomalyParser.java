package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.ftp.SceneAnomalyData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：SceneAnomalyParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:37
 * @Description: 作业场景异常数据解析器（附录C.2.2）
 */
@Component
public class SceneAnomalyParser implements DataParser<SceneAnomalyData> {

    @Override
    public String parse(SceneAnomalyData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getCameraSn(),
                data.getAnomalyType(),
                data.getAnomalyTime(),
                data.getHandleResult(),
                data.getDataTime()
        );
    }

}
