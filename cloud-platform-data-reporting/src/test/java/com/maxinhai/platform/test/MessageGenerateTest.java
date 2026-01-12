package com.maxinhai.platform.test;

import cn.hutool.core.date.DateUtil;
import com.maxinhai.platform.po.mq.*;
import com.maxinhai.platform.util.CodeGenerateUtil;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.UUID;

/**
 * @ClassName：MessageGenerateTest
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 3:10
 * @Description: 消息队列报文模型测试类，核心：验证报文模型 buildMessage() 方法的转换正确性，使用 Spring Assert 断言
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class MessageGenerateTest {

    // ====================== 补全中断的常量定义（无代码中断） ======================
    // 父类消息头通用常量（必填）
    private static final String MINE_CODE = "140421007866";          // 12位煤矿编码
    private static final String MINE_NAME = "XX煤矿（井工）";        // 煤矿名称
    private static final int MINE_TYPE = 1;                         // 1=井工矿（父类必填）
    private static final String UPLOAD_TIME = DateUtil.format(new Date(), "yyyyMMddHHmmss"); // 14位上传时间

    // 补全中断的 AREA_CODE 及核心业务编码
    private static final String AREA_CODE = "01001";                // 5位区域编码（补全中断定义）
    private static final String VALID_8BIT_DATE = UPLOAD_TIME.substring(0, 8); // 8位日期
    // 动态生成合规业务编码（替代硬编码）
    private static final String LOCATION_CODE = CodeGenerateUtil.generateLocationCode(AREA_CODE); // 9位位置编码
    private static final String POINT_CODE = CodeGenerateUtil.generatePointCode(AREA_CODE, LOCATION_CODE); // 14位点位编码
    private static final String CAMERA_CODE = CodeGenerateUtil.generateCameraStandardCode(); // 20位摄像仪国标编码

    /**
     * 测试1：安装位置基础信息报文生成
     */
    @Test
    public void testInstallLocationBasicInfo() {
        // 1. 构造实体（父类+子类字段全赋值）
        InstallLocationBasicInfo entity = new InstallLocationBasicInfo();
        // 父类消息头字段
        entity.setMineCode(MINE_CODE);
        entity.setMineName(MINE_NAME);
        entity.setMineType(MINE_TYPE);
        entity.setUploadTime(UPLOAD_TIME);
        // 子类消息体字段
        entity.setAreaCode(AREA_CODE);
        entity.setLocationCode(LOCATION_CODE);
        entity.setLocationName("主井绞车房");
        entity.setIsInvolved(1);

        // 2. 生成报文
        String message = entity.buildMessage();

        // 3. Spring Assert 断言验证
        Assert.hasLength(message, "安装位置基础信息报文不能为空");
        Assert.isTrue(message.contains(MINE_CODE) && message.contains(UPLOAD_TIME), "父类消息头字段未正确赋值");
        Assert.isTrue(message.contains(LOCATION_CODE), "消息体位置编码字段未正确赋值");
    }

    /**
     * 测试2：点位基础信息报文生成
     */
    @Test
    public void testPointBasicInfo() {
        // 1. 构造实体
        PointBasicInfo entity = new PointBasicInfo();
        // 父类消息头字段
        entity.setMineCode(MINE_CODE);
        entity.setMineName(MINE_NAME);
        entity.setMineType(MINE_TYPE);
        entity.setUploadTime(UPLOAD_TIME);
        // 子类消息体字段
        entity.setPointCode(POINT_CODE);
        entity.setPointName("主井绞车房-01");
        entity.setPointAbility("A001&A002");
        entity.setPointStatus(1);
        entity.setCreationTime(UPLOAD_TIME);

        // 2. 生成报文
        String message = entity.buildMessage();

        // 3. Spring Assert 断言验证
        Assert.hasLength(message, "点位基础信息报文不能为空");
        Assert.isTrue(message.contains(POINT_CODE), "14位点位编码未正确赋值");
        Assert.isTrue(message.contains("pointAbility=A001&A002"), "多能力拼接字段未正确赋值");
    }

    /**
     * 测试3：点位摄像仪关联信息报文生成
     */
    @Test
    public void testPointCameraMapping() {
        // 1. 构造实体
        PointCameraMappingInfo entity = new PointCameraMappingInfo();
        // 父类消息头字段
        entity.setMineCode(MINE_CODE);
        entity.setMineName(MINE_NAME);
        entity.setMineType(MINE_TYPE);
        entity.setUploadTime(UPLOAD_TIME);
        // 子类消息体字段
        entity.setPointCode(POINT_CODE);
        entity.setCameraStandardCode(CAMERA_CODE);
        entity.setCameraName("主井绞车房摄像仪01");
        entity.setPointBaseImgUrl("rtsp://10.2.3.4/stream1");

        // 2. 生成报文
        String message = entity.buildMessage();

        // 3. Spring Assert 断言验证
        Assert.hasLength(message, "点位摄像仪关联报文不能为空");
        Assert.isTrue(message.contains(CAMERA_CODE), "20位摄像仪国标编码未正确赋值");
        Assert.isTrue(message.contains("rtsp://10.2.3.4/stream1"), "基准图片URL字段未正确赋值");
    }

    /**
     * 测试4：点位摄像仪实时数据报文生成
     */
    @Test
    public void testPointCameraRealTime() {
        // 1. 构造实体
        PointCameraRealTimeData entity = new PointCameraRealTimeData();
        // 父类消息头字段
        entity.setMineCode(MINE_CODE);
        entity.setMineName(MINE_NAME);
        entity.setMineType(MINE_TYPE);
        entity.setUploadTime(UPLOAD_TIME);
        // 子类消息体字段
        entity.setPointCode(POINT_CODE);
        entity.setCameraStandardCode(CAMERA_CODE);
        entity.setOnlineStatus(1);
        entity.setDataGenerateTime(UPLOAD_TIME);

        // 2. 生成报文
        String message = entity.buildMessage();

        // 3. Spring Assert 断言验证
        Assert.hasLength(message, "摄像仪实时数据报文不能为空");
        Assert.isTrue(message.contains("onlineStatus=1"), "在线状态字段未正确赋值");
        Assert.isTrue(message.contains(UPLOAD_TIME), "采集时间字段未正确赋值");
    }

    /**
     * 测试5：录像存储状况巡检报文生成
     */
    @Test
    public void testRecordingStorageInspection() {
        // 1. 构造实体
        RecordingStorageInspection entity = new RecordingStorageInspection();
        // 父类消息头字段
        entity.setMineCode(MINE_CODE);
        entity.setMineName(MINE_NAME);
        entity.setMineType(MINE_TYPE);
        entity.setUploadTime(UPLOAD_TIME);
        // 子类消息体字段
        entity.setPointCode(POINT_CODE);
        entity.setInspectionDate(VALID_8BIT_DATE);
        entity.setStorageDayQualified(1);
        entity.setShouldStoreDateRange("20240628-20240629&20240629-20240630");

        // 2. 生成报文
        String message = entity.buildMessage();

        // 3. Spring Assert 断言验证
        Assert.hasLength(message, "存储巡检报文不能为空");
        Assert.isTrue(message.contains(VALID_8BIT_DATE), "8位巡检日期未正确赋值");
        Assert.isTrue(message.contains("storageDayQualified=1"), "存储达标字段未正确赋值");
    }

    /**
     * 测试6：画面质量异常数据报文生成
     */
    @Test
    public void testVideoQualityAnomaly() {
        // 1. 构造实体
        VideoQualityAnomalyData entity = new VideoQualityAnomalyData();
        // 父类消息头字段
        entity.setMineCode(MINE_CODE);
        entity.setMineName(MINE_NAME);
        entity.setMineType(MINE_TYPE);
        entity.setUploadTime(UPLOAD_TIME);
        // 子类消息体字段
        entity.setPointCode(POINT_CODE);
        entity.setExceptionCode(CodeGenerateUtil.generateVideoExceptionCode());
        entity.setEventCode("A001");
        entity.setStartTime(UPLOAD_TIME);
        entity.setBaseImgUrl("rtsp://10.2.3.4/base.jpg");

        // 2. 生成报文
        String message = entity.buildMessage();

        // 3. Spring Assert 断言验证
        Assert.hasLength(message, "画面异常报文不能为空");
        Assert.isTrue(message.contains("eventCode=A001"), "异常类型编码未正确赋值");
        Assert.isTrue(message.contains("HM" + VALID_8BIT_DATE), "14位画面异常编码未正确生成");
    }

    /**
     * 测试7：作业场景异常数据报文生成
     */
    @Test
    public void testSceneAnomaly() {
        // 1. 构造实体
        SceneAnomalyData entity = new SceneAnomalyData();
        // 父类消息头字段
        entity.setMineCode(MINE_CODE);
        entity.setMineName(MINE_NAME);
        entity.setMineType(MINE_TYPE);
        entity.setUploadTime(UPLOAD_TIME);
        // 子类消息体字段
        entity.setPointCode(POINT_CODE);
        entity.setExceptionCode(CodeGenerateUtil.generateSceneExceptionCode());
        entity.setEventCode("B005");
        entity.setStartTime(UPLOAD_TIME);
        entity.setEventDesc("作业人员未佩戴安全帽");

        // 2. 生成报文
        String message = entity.buildMessage();

        // 3. Spring Assert 断言验证
        Assert.hasLength(message, "场景异常报文不能为空");
        Assert.isTrue(message.contains("eventCode=B005"), "场景异常类型编码未正确赋值");
        Assert.isTrue(message.contains("eventDesc=作业人员未佩戴安全帽"), "异常描述字段未正确赋值");
    }

    /**
     * 测试8：摄像仪挪移日志报文生成
     */
    @Test
    public void testCameraMovementLog() {
        // 1. 构造实体
        CameraMovementLog entity = new CameraMovementLog();
        // 父类消息头字段
        entity.setMineCode(MINE_CODE);
        entity.setMineName(MINE_NAME);
        entity.setMineType(MINE_TYPE);
        entity.setUploadTime(UPLOAD_TIME);
        // 子类消息体字段
        entity.setPointCode(POINT_CODE);
        entity.setCameraStandardCode(CAMERA_CODE);
        entity.setChangeType(0);
        entity.setMovementReason("设备搬家倒面");
        entity.setMovementTime(UPLOAD_TIME);

        // 2. 生成报文
        String message = entity.buildMessage();

        // 3. Spring Assert 断言验证
        Assert.hasLength(message, "挪移日志报文不能为空");
        Assert.isTrue(message.contains("changeType=0"), "挪移类型字段未正确赋值");
        Assert.isTrue(message.contains("movementReason=设备搬家倒面"), "挪移原因字段未正确赋值");
    }

    /**
     * 测试9：报备数据报文生成
     */
    @Test
    public void testReportData() {
        // 1. 构造实体
        ReportData entity = new ReportData();
        // 父类消息头字段
        entity.setMineCode(MINE_CODE);
        entity.setMineName(MINE_NAME);
        entity.setMineType(MINE_TYPE);
        entity.setUploadTime(UPLOAD_TIME);
        // 子类消息体字段
        entity.setPointCode(POINT_CODE);
        entity.setReportStartTime("20240629080000");
        entity.setReportEndTime("20240629180000");
        entity.setReportReason("设备检修");

        // 2. 生成报文
        String message = entity.buildMessage();

        // 3. Spring Assert 断言验证
        Assert.hasLength(message, "报备数据报文不能为空");
        Assert.isTrue(message.contains("reportReason=设备检修"), "报备原因字段未正确赋值");
        Assert.isTrue(message.contains("20240629080000"), "报备开始时间字段未正确赋值");
    }

    /**
     * 测试10：移动作业施工记录报文生成
     */
    @Test
    public void testMobileWorkRecord() {
        // 1. 构造实体
        MobileWorkRecord entity = new MobileWorkRecord();
        // 父类消息头字段
        entity.setMineCode(MINE_CODE);
        entity.setMineName(MINE_NAME);
        entity.setMineType(MINE_TYPE);
        entity.setUploadTime(UPLOAD_TIME);
        // 子类消息体字段
        entity.setWorkTypeCode("03");
        entity.setWorkCode(UUID.randomUUID().toString().replace("-", ""));
        entity.setDesignCode(CodeGenerateUtil.generateDesignCode());
        entity.setHoleNum(CodeGenerateUtil.generateHoleNum());
        entity.setVideoUrl("rtsp://10.2.3.4/stream1|rtsp://10.2.3.4/stream2");

        // 2. 生成报文
        String message = entity.buildMessage();

        // 3. Spring Assert 断言验证
        Assert.hasLength(message, "移动作业报文不能为空");
        Assert.isTrue(message.contains("workTypeCode=03"), "作业类型编码未正确赋值");
        Assert.isTrue(message.contains("|"), "多段视频URL拼接字段未正确赋值");
    }

}
