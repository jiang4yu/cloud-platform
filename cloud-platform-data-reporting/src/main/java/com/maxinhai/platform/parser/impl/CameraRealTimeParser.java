package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.CameraRealTimeData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：CameraRealTimeParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:36
 * @Description: 摄像仪实时数据解析器（附录B.1.4）
 */
@Component
public class CameraRealTimeParser implements DataParser<CameraRealTimeData> {

    @Override
    public String parse(CameraRealTimeData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getCameraSn(),
                data.getOnlineStatus(),
                data.getVideoQuality(),
                data.getStorageStatus(),
                data.getDataTime()
        );
    }
}
