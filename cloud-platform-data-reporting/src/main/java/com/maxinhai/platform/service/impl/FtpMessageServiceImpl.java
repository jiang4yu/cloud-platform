package com.maxinhai.platform.service.impl;

import com.maxinhai.platform.enums.FtpFileTypeEnum;
import com.maxinhai.platform.parser.DataParser;
import com.maxinhai.platform.parser.ParserFactory;
import com.maxinhai.platform.po.ftp.*;
import com.maxinhai.platform.service.FtpMessageService;
import com.maxinhai.platform.util.FtpFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName：FtpMessageServiceImpl
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:48
 * @Description: FTP报文服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FtpMessageServiceImpl implements FtpMessageService {

    private final FtpFileUtil ftpFileUtil;
    private final ParserFactory parserFactory;

    @Override
    public boolean sendInstallPosition(String coalMineCode, int mineType, List<InstallPositionData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.INSTALL_POSITION, dataList);
    }

    @Override
    public boolean sendPointBasic(String coalMineCode, int mineType, List<PointBasicData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.POINT_BASIC, dataList);
    }

    @Override
    public boolean sendPointCameraMapping(String coalMineCode, int mineType, List<PointCameraMappingData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.POINT_CAMERA_MAPPING, dataList);
    }

    @Override
    public boolean sendCameraRealTime(String coalMineCode, int mineType, List<CameraRealTimeData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.CAMERA_REAL_TIME, dataList);
    }

    @Override
    public boolean sendRecordStorageCheck(String coalMineCode, int mineType, List<RecordStorageCheckData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.RECORD_STORAGE_CHECK, dataList);
    }

    @Override
    public boolean sendAiRecognition(String coalMineCode, int mineType, List<AiRecognitionData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.AI_RECOGNITION, dataList);
    }

    @Override
    public boolean sendVideoAnomaly(String coalMineCode, int mineType, List<VideoAnomalyData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.VIDEO_ANOMALY, dataList);
    }

    @Override
    public boolean sendSceneAnomaly(String coalMineCode, int mineType, List<SceneAnomalyData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.SCENE_ANOMALY, dataList);
    }

    @Override
    public boolean sendCameraMovement(String coalMineCode, int mineType, List<CameraMovementData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.CAMERA_MOVEMENT, dataList);
    }

    @Override
    public boolean sendReportData(String coalMineCode, int mineType, List<ReportData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.REPORT_DATA, dataList);
    }

    @Override
    public boolean sendDrillDesign(String coalMineCode, int mineType, List<DrillDesignData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.DRILL_DESIGN, dataList);
    }

    @Override
    public boolean sendMobileWorkRecord(String coalMineCode, int mineType, List<MobileWorkRecordData> dataList) {
        return sendFtpMessage(coalMineCode, mineType, FtpFileTypeEnum.MOBILE_WORK_RECORD, dataList);
    }

    @Override
    public <T> boolean sendFtpMessage(String coalMineCode, int mineType, FtpFileTypeEnum type, List<T> dataList) {
        try {
            // 1. 获取解析器
            DataParser<T> parser = parserFactory.getParser(type);
            if (parser == null) {
                log.error("未找到{}对应的解析器", type.getName());
                return false;
            }

            // 2. 生成本地文件
            String localPath = ftpFileUtil.generateFtpFile(coalMineCode, mineType, type, dataList, parser);

            // 3. 上传FTP
            return ftpFileUtil.uploadSmallFile(localPath);
        } catch (Exception e) {
            log.error("发送{}报文失败", type.getName(), e);
            return false;
        }
    }

}
