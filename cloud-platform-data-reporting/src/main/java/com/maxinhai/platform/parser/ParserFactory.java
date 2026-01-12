package com.maxinhai.platform.parser;

import com.maxinhai.platform.enums.FtpFileTypeEnum;
import com.maxinhai.platform.parser.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName：ParserFactory
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:35
 * @Description: 解析器工厂（根据报文类型获取对应解析器）
 */
@Component
public class ParserFactory {

    // 解析器映射
    private final Map<FtpFileTypeEnum, DataParser<?>> parserMap = new HashMap<>();

    @Autowired
    public ParserFactory(
            InstallPositionParser installPositionParser,
            PointBasicParser pointBasicParser,
            PointCameraMappingParser pointCameraMappingParser,
            CameraRealTimeParser cameraRealTimeParser,
            RecordStorageCheckParser recordStorageCheckParser,
            AiRecognitionParser aiRecognitionParser,
            VideoAnomalyParser videoAnomalyParser,
            SceneAnomalyParser sceneAnomalyParser,
            CameraMovementParser cameraMovementParser,
            ReportDataParser reportDataParser,
            DrillDesignParser drillDesignParser,
            MobileWorkRecordParser mobileWorkRecordParser
    ) {
        // 注册12类解析器
        parserMap.put(FtpFileTypeEnum.INSTALL_POSITION, installPositionParser);
        parserMap.put(FtpFileTypeEnum.POINT_BASIC, pointBasicParser);
        parserMap.put(FtpFileTypeEnum.POINT_CAMERA_MAPPING, pointCameraMappingParser);
        parserMap.put(FtpFileTypeEnum.CAMERA_REAL_TIME, cameraRealTimeParser);
        parserMap.put(FtpFileTypeEnum.RECORD_STORAGE_CHECK, recordStorageCheckParser);
        parserMap.put(FtpFileTypeEnum.AI_RECOGNITION, aiRecognitionParser);
        parserMap.put(FtpFileTypeEnum.VIDEO_ANOMALY, videoAnomalyParser);
        parserMap.put(FtpFileTypeEnum.SCENE_ANOMALY, sceneAnomalyParser);
        parserMap.put(FtpFileTypeEnum.CAMERA_MOVEMENT, cameraMovementParser);
        parserMap.put(FtpFileTypeEnum.REPORT_DATA, reportDataParser);
        parserMap.put(FtpFileTypeEnum.DRILL_DESIGN, drillDesignParser);
        parserMap.put(FtpFileTypeEnum.MOBILE_WORK_RECORD, mobileWorkRecordParser);
    }

    /**
     * 获取指定类型的解析器
     */
    @SuppressWarnings("unchecked")
    public <T> DataParser<T> getParser(FtpFileTypeEnum type) {
        return (DataParser<T>) parserMap.get(type);
    }

}
