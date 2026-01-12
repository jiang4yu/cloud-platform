package com.maxinhai.platform.test;

import com.maxinhai.platform.po.*;
import com.maxinhai.platform.service.FtpMessageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName：FtpAllMessageTest
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 22:51
 * @Description: 12类FTP报文全量测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FtpAllMessageTest {

    @Autowired
    private FtpMessageService ftpMessageService;

    // 基础测试参数（请替换为真实值）
    private static final String COAL_MINE_CODE = "610000000001"; // 12位煤矿编码
    private static final int MINE_TYPE = 1; // 1=井工矿，2=露天矿
    private static final String TEST_TIME = "20250612100000"; // 标准时间格式yyyyMMddHHmmss
    private static final int BATCH_SIZE = 1;

    // ==================== B类：基础数据测试 ====================
    /**
     * 1. 安装位置基础信息测试
     */
    @Test
    public void testInstallPosition() {
        List<InstallPositionData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            InstallPositionData data = new InstallPositionData();
            data.setAreaName("2056采煤工作面");
            data.setAreaCode("01001"); // 5位区域编码
            data.setAreaStatus(1); // 1=使用
            data.setPositionClassCode("01"); // 附录A分类编码
            data.setIsInvolved(1); // 1=涉及
            data.setPositionName("2056采面50架摄像头位置");
            data.setPositionCode("010010101"); // 9位安装位置编码
            data.setPositionStatus(1); // 1=使用
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }


        boolean result = ftpMessageService.sendInstallPosition(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【安装位置基础信息】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 2. 点位基础信息测试
     */
    @Test
    public void testPointBasic() {
        List<PointBasicData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            PointBasicData data = new PointBasicData();
            data.setPointCode("01001010100001"); // 14位点位编码
            data.setPointName("2056采面50架摄像头点位");
            data.setPointStatus(1); // 1=使用
            data.setPointAbilities(Arrays.asList("01", "02")); // 点位能力（多值用&分隔）
            data.setCreationTime(TEST_TIME);
            data.setConfirmStatus(1); // 1=已确认
            data.setConfirmPerson("张三");
            data.setConfirmTime(TEST_TIME);
            data.setPositioningSubstationCode("88001");
            data.setPositioningSubstationName("2056采面分站");
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendPointBasic(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【点位基础信息】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 3. 点位摄像仪关联信息测试
     */
    @Test
    public void testPointCameraMapping() {
        List<PointCameraMappingData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            PointCameraMappingData data = new PointCameraMappingData();
            data.setPointCode("01001010100001"); // 14位点位编码
            data.setCameraVendor("海康威视");
            data.setCameraModel("DS-2CD3T46WD-I3");
            data.setCameraSn("SN1234567890123"); // 摄像仪唯一序列号
            data.setCameraIp("192.168.1.101");
            data.setCameraPort(8000);
            data.setPlatformCode("SX_COAL_001"); // 接入平台编码
            data.setMappingStatus(1); // 1=关联
            data.setMappingTime(TEST_TIME);
            data.setUnMappingTime(""); // 非必填
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendPointCameraMapping(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【点位摄像仪关联信息】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 4. 摄像仪实时数据测试
     */
    @Test
    public void testCameraRealTime() {
        List<CameraRealTimeData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            CameraRealTimeData data = new CameraRealTimeData();
            data.setCameraSn("SN1234567890123");
            data.setOnlineStatus("1"); // 1=在线，0=离线
            data.setVideoQuality("2"); // 2=优，1=中，0=差
            data.setStorageStatus("1"); // 1=正常，0=异常
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendCameraRealTime(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【摄像仪实时数据】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 5. 录像存储状况巡检测试
     */
    @Test
    public void testRecordStorageCheck() {
        List<RecordStorageCheckData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            RecordStorageCheckData data = new RecordStorageCheckData();
            data.setCameraSn("SN1234567890123");
            data.setCheckTime(TEST_TIME);
            data.setStorageDays("30"); // 存储天数（整数）
            data.setCheckResult("1"); // 1=正常，0=异常
            data.setExceptionDesc(""); // 无异常则为空
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendRecordStorageCheck(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【录像存储状况巡检】测试结果：" + (result ? "成功" : "失败"));
    }

    // ==================== C类：异常/运维/专项数据测试 ====================
    /**
     * 6. 作业场景识别数据测试
     */
    @Test
    public void testAiRecognition() {
        List<AiRecognitionData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            AiRecognitionData data = new AiRecognitionData();
            data.setCameraSn("SN1234567890123");
            data.setSceneType("01"); // 附录C.1场景类型（01=人员未戴安全帽）
            data.setRecognitionTime(TEST_TIME);
            data.setRecognitionResult("1"); // 1=识别到，0=未识别到
            data.setPicUrl("http://192.168.1.100/pic/20250612100001.jpg"); // 截图URL
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendAiRecognition(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【作业场景识别数据】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 7. 画面质量异常数据测试
     */
    @Test
    public void testVideoAnomaly() {
        List<VideoAnomalyData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            VideoAnomalyData data = new VideoAnomalyData();
            data.setCameraSn("SN1234567890123");
            data.setAnomalyType("01"); // 01=花屏，02=黑屏，03=卡顿
            data.setAnomalyTime(TEST_TIME);
            data.setRecoverTime("20250612103000"); // 恢复时间
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendVideoAnomaly(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【画面质量异常数据】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 8. 作业场景异常数据测试
     */
    @Test
    public void testSceneAnomaly() {
        List<SceneAnomalyData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            SceneAnomalyData data = new SceneAnomalyData();
            data.setCameraSn("SN1234567890123");
            data.setAnomalyType("02"); // 附录C.2异常类型（02=人员闯入危险区域）
            data.setAnomalyTime(TEST_TIME);
            data.setHandleResult("已通知现场安全员处理"); // 处理结果
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendSceneAnomaly(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【作业场景异常数据】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 9. 摄像仪挪移日志测试
     */
    @Test
    public void testCameraMovement() {
        List<CameraMovementData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            CameraMovementData data = new CameraMovementData();
            data.setCameraSn("SN1234567890123");
            data.setOldPointCode("01001010100001"); // 原点位编码
            data.setNewPointCode("01001010100002"); // 新点位编码
            data.setMoveTime(TEST_TIME);
            data.setOperator("李四"); // 操作人
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendCameraMovement(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【摄像仪挪移日志】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 10. 报备数据测试
     */
    @Test
    public void testReportData() {
        List<ReportData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            ReportData data = new ReportData();
            data.setReportType("01"); // 01=安装，02=挪移，03=拆除
            data.setReportTime(TEST_TIME);
            data.setApproveStatus("1"); // 1=通过，0=未审批，2=驳回
            data.setApproveTime(TEST_TIME);
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendReportData(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【报备数据】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 11. 重大灾害钻孔施工设计信息测试
     */
    @Test
    public void testDrillDesign() {
        List<DrillDesignData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            DrillDesignData data = new DrillDesignData();
            data.setDrillCode("ZK20250612001"); // 钻孔编码
            data.setDrillType("01"); // 附录C.3钻孔类型（01=瓦斯抽采孔）
            data.setDesignDepth("150.5"); // 设计深度（数值）
            data.setConstructionUnit("陕西XX煤矿工程有限公司"); // 施工单位
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendDrillDesign(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【重大灾害钻孔施工设计信息】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 12. 移动作业施工记录信息测试
     */
    @Test
    public void testMobileWorkRecord() {
        List<MobileWorkRecordData> dataList = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            MobileWorkRecordData data = new MobileWorkRecordData();
            data.setWorkCode("YD20250612001"); // 作业编码
            data.setWorkType("01"); // 附录C.4作业类型（01=移动摄像仪安装）
            data.setWorkStartTime(TEST_TIME);
            data.setWorkEndTime("20250612180000"); // 作业结束时间
            data.setWorkResult("1"); // 1=成功，0=失败
            data.setDataTime(TEST_TIME);
            dataList.add(data);
        }

        boolean result = ftpMessageService.sendMobileWorkRecord(COAL_MINE_CODE, MINE_TYPE, dataList);
        System.out.println("【移动作业施工记录信息】测试结果：" + (result ? "成功" : "失败"));
    }

    /**
     * 批量测试所有12类报文（一键执行）
     */
    @Test
    public void testAllMessages() throws InterruptedException {
        testInstallPosition();
        TimeUnit.SECONDS.sleep(3);
        testPointBasic();
        TimeUnit.SECONDS.sleep(3);
        testPointCameraMapping();
        TimeUnit.SECONDS.sleep(3);
        testCameraRealTime();
        TimeUnit.SECONDS.sleep(3);
        testRecordStorageCheck();
        TimeUnit.SECONDS.sleep(3);
        testAiRecognition();
        TimeUnit.SECONDS.sleep(3);
        testVideoAnomaly();
        TimeUnit.SECONDS.sleep(3);
        testSceneAnomaly();
        TimeUnit.SECONDS.sleep(3);
        testCameraMovement();
        TimeUnit.SECONDS.sleep(3);
        testReportData();
        TimeUnit.SECONDS.sleep(3);
        testDrillDesign();
        TimeUnit.SECONDS.sleep(3);
        testMobileWorkRecord();
        System.out.println("==================== 全量12类FTP报文测试执行完成 ====================");
    }

}
