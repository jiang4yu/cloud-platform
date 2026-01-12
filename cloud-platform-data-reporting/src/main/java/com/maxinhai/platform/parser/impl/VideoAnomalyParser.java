package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.ftp.VideoAnomalyData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：VideoAnomalyParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:37
 * @Description: 画面质量异常数据解析器（附录C.2.1）
 */
@Component
public class VideoAnomalyParser implements DataParser<VideoAnomalyData> {

    @Override
    public String parse(VideoAnomalyData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getCameraSn(),
                data.getAnomalyType(),
                data.getAnomalyTime(),
                data.getRecoverTime(),
                data.getDataTime()
        );
    }

}
