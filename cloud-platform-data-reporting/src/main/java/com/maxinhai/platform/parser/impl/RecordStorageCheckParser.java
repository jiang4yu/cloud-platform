package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.ftp.RecordStorageCheckData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：RecordStorageCheckParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:36
 * @Description: 录像存储状况巡检解析器（附录B.1.5）
 */
@Component
public class RecordStorageCheckParser implements DataParser<RecordStorageCheckData> {

    @Override
    public String parse(RecordStorageCheckData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getCameraSn(),
                data.getCheckTime(),
                data.getStorageDays(),
                data.getCheckResult(),
                data.getExceptionDesc(),
                data.getDataTime()
        );
    }

}
