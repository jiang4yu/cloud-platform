package com.maxinhai.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataTypeEnum {

    INSTALL_LOCATION_BASIC("install-location-basic", "minemonitordata_spjk_locationbasicinfo"),
    POINT_BASIC("point-basic", "minemonitordata_spjk_pointbasicinfo"),
    POINT_CAMERA_MAPPING("point-camera-mapping", "minemonitordata_spjk_pointcameramapping"),
    POINT_CAMERA_STATUS("point-camera-status", "minemonitordata_spjk_pointcamerastatus"),
    RECORDING_STATUS("recording-status", "minemonitordata_spjk_recordingstatus"),
    VIDEO_ANOMALY("video-anomaly", "minemonitordata_spjk_videoanomaly"),
    SCENE_ANOMALY("scene-anomaly", "minemonitordata_spjk_sceneanomaly"),
    MOVEMENT_RECORDS("movement-records", "minemonitordata_spjk_movementrecords"),
    REPORT_RECORDS("report-records", "minemonitordata_spjk_reportrecords"),
    MOBILE_WORK_RECORD("mobile-work-record", "minemonitordata_spjk_mobile_workrecord");

    // 数据类型标识（与业务层对齐）
    private final String dataTypeKey;
    // 对应的消息队列名称
    private final String queueName;

    // 根据数据类型key获取枚举
    public static DataTypeEnum getByDataTypeKey(String dataTypeKey) {
        for (DataTypeEnum enumItem : values()) {
            if (enumItem.getDataTypeKey().equals(dataTypeKey)) {
                return enumItem;
            }
        }
        throw new IllegalArgumentException("未找到对应的数据类型：" + dataTypeKey);
    }

}
