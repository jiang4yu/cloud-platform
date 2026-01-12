package com.maxinhai.platform.parser.impl;

import com.maxinhai.platform.constant.FtpConstant;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.po.AiRecognitionData;
import org.springframework.stereotype.Component;

/**
 * @ClassName：AiRecognitionParser
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:37
 * @Description: 作业场景识别数据解析器（附录C.1）
 */
@Component
public class AiRecognitionParser implements DataParser<AiRecognitionData> {

    @Override
    public String parse(AiRecognitionData data) {
        return String.join(FtpConstant.FIELD_SEPARATOR,
                data.getCameraSn(),
                data.getSceneType(),
                data.getRecognitionTime(),
                data.getRecognitionResult(),
                data.getPicUrl(),
                data.getDataTime()
        );
    }

}
