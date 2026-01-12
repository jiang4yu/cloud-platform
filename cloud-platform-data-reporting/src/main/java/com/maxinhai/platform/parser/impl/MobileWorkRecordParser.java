package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.ftp.MobileWorkRecordData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：MobileWorkRecordParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:37
 * @Description: 移动作业施工记录信息解析器（附录C.6）
 */
@Component
public class MobileWorkRecordParser implements DataParser<MobileWorkRecordData> {

    @Override
    public String parse(MobileWorkRecordData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getWorkCode(),
                data.getWorkType(),
                data.getWorkStartTime(),
                data.getWorkEndTime(),
                data.getWorkResult(),
                data.getDataTime()
        );
    }

}
