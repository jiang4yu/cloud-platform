package com.maxinhai.platform.enums;

import lombok.Getter;

/**
 * 12类FTP报文类型枚举（严格对应细则附录B/C）
 */
@Getter
public enum FtpFileTypeEnum {

    // B类：基础数据
    INSTALL_POSITION("AZWZJC", "安装位置基础信息"),
    POINT_BASIC("DWJC", "点位基础信息"),
    POINT_CAMERA_MAPPING("DWSXYGL", "点位摄像仪关联信息"),
    CAMERA_REAL_TIME("DWSXYSS", "摄像仪实时数据"),
    RECORD_STORAGE_CHECK("LXCCXJ", "录像存储状况巡检"),
    // C类：异常/运维/专项数据
    AI_RECOGNITION("AIJG", "作业场景识别数据"),
    VIDEO_ANOMALY("HMYC", "画面质量异常数据"),
    SCENE_ANOMALY("CJYC", "作业场景异常数据"),
    CAMERA_MOVEMENT("SXYNY", "摄像仪挪移日志"),
    REPORT_DATA("BAB", "报备数据"),
    DRILL_DESIGN("ZKSJ", "重大灾害钻孔施工设计信息"),
    MOBILE_WORK_RECORD("YDZYSGJL", "移动作业施工记录信息");

    // 报文类型编码（文件名用）
    private final String code;
    // 报文名称（日志/注释用）
    private final String name;

    FtpFileTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
