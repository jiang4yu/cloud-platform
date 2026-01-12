package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.ftp.ReportData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：ReportDataParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:37
 * @Description: 报备数据解析器（附录C.4）
 */
@Component
public class ReportDataParser implements DataParser<ReportData> {

    @Override
    public String parse(ReportData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getReportType(),
                data.getReportTime(),
                data.getApproveStatus(),
                data.getApproveTime(),
                data.getDataTime()
        );
    }

}
