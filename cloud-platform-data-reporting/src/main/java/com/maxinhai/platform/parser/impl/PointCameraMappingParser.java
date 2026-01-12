package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.ftp.PointCameraMappingData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：PointCameraMappingParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:36
 * @Description: 点位摄像仪关联信息解析器（附录B.1.3）
 */
@Component
public class PointCameraMappingParser implements DataParser<PointCameraMappingData> {

    @Override
    public String parse(PointCameraMappingData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getPointCode(),
                data.getCameraVendor(),
                data.getCameraModel(),
                data.getCameraSn(),
                data.getCameraIp(),
                data.getCameraPort().toString(),
                data.getPlatformCode(),
                data.getMappingStatus().toString(),
                data.getMappingTime(),
                data.getUnMappingTime(),
                data.getDataTime()
        );
    }

}
