package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：RecordingStorageInspection
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:25
 * @Description: 对应附录B.1.3.15 录像存储状况巡检
 */
@Data
@Accessors(chain = true)
public class RecordingStorageInspection extends BaseMessageEntity {

    // 消息体（必填）
    private String pointCode; // 14位点位编码
    private String inspectionDate; // 巡检日期（8位：YYYYMMDD）
    private int storageDayQualified; // 0=不达标/1=达标/2=不涉及
    private String shouldStoreDateRange; // 应保存日期区间（多个用&分隔，如"20240331-20240629"）
    private String actualStoreDate; // 实际保存日期区间（格式同上）
    private int inspectionIntegrityQualified; // 0=不达标/1=达标
    private String shouldStoreTimeRange; // 应保存时间段（多个用&分隔，如"000000-235959"）
    private String actualStoreTimeRange; // 实际保存时间段（格式同上）
    private String dataGenerateTime; // 数据生成时间（14位）

    /**
     * 构建报文字符串
     */
    @Override
    public String buildMessage() {
        String header = String.format("%s;%s;%d;%s", getMineCode(), getMineName(), getMineType(), getUploadTime());
        String body = String.format("%s;%s;%d;%s;%s;%d;%s;%s;%s",
                pointCode, inspectionDate, storageDayQualified, shouldStoreDateRange,
                actualStoreDate, inspectionIntegrityQualified, shouldStoreTimeRange,
                actualStoreTimeRange, dataGenerateTime);
        return header + "~" + body + "~||";
    }

}
