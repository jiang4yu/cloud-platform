package com.maxinhai.platform.service;

import com.maxinhai.platform.enums.FtpFileTypeEnum;
import com.maxinhai.platform.po.*;

import java.util.List;

/**
 * @ClassName：FtpMessageService
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:47
 * @Description: FTP报文统一服务接口
 */
public interface FtpMessageService {

    // 安装位置基础信息
    boolean sendInstallPosition(String coalMineCode, int mineType, List<InstallPositionData> dataList);
    // 点位基础信息
    boolean sendPointBasic(String coalMineCode, int mineType, List<PointBasicData> dataList);
    // 点位摄像仪关联信息
    boolean sendPointCameraMapping(String coalMineCode, int mineType, List<PointCameraMappingData> dataList);
    // 摄像仪实时数据
    boolean sendCameraRealTime(String coalMineCode, int mineType, List<CameraRealTimeData> dataList);
    // 录像存储巡检
    boolean sendRecordStorageCheck(String coalMineCode, int mineType, List<RecordStorageCheckData> dataList);
    // AI识别数据
    boolean sendAiRecognition(String coalMineCode, int mineType, List<AiRecognitionData> dataList);
    // 画面质量异常
    boolean sendVideoAnomaly(String coalMineCode, int mineType, List<VideoAnomalyData> dataList);
    // 作业场景异常
    boolean sendSceneAnomaly(String coalMineCode, int mineType, List<SceneAnomalyData> dataList);
    // 摄像仪挪移日志
    boolean sendCameraMovement(String coalMineCode, int mineType, List<CameraMovementData> dataList);
    // 报备数据
    boolean sendReportData(String coalMineCode, int mineType, List<ReportData> dataList);
    // 钻孔施工设计
    boolean sendDrillDesign(String coalMineCode, int mineType, List<DrillDesignData> dataList);
    // 移动作业记录
    boolean sendMobileWorkRecord(String coalMineCode, int mineType, List<MobileWorkRecordData> dataList);

    /**
     * 通用发送方法（适配所有类型）
     */
    <T> boolean sendFtpMessage(String coalMineCode, int mineType, FtpFileTypeEnum type, List<T> dataList);

}
