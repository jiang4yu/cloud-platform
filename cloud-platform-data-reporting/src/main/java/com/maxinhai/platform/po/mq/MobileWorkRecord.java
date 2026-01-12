package com.maxinhai.platform.po.mq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName：MobileWorkRecord
 * @Author: XinHai.Ma
 * @Date: 2026/1/13 2:15
 * @Description: 移动作业施工记录信息（对应附录C.2）
 */
@Data
@Accessors(chain = true)
public class MobileWorkRecord extends BaseMessageEntity {

    // 消息体字段（必填+可选）
    private String workCode; // 作业编码（UUID）
    private String designCode; // 施工设计编码（非必填）
    private String workTypeCode; // 作业类型编号（01-08）
    private String workPosition; // 施工位置
    private String holeNum; // 孔号（非必填）
    private String workDate; // 施工日期（8位）
    private String workClass; // 施工班次
    private int drillingCount; // 进杆数量（非必填）
    private int retractCount; // 退杆数量（非必填）
    private String workMaster; // 施工机长（非必填）
    private String videoUrl; // 作业录像URL
    private String acceptor; // 验收人（非必填）
    private String acceptConclusion; // 验收结论（非必填）
    private String reporter; // 填报人
    private String reportTime; // 填报时间（14位）
    private String dataGenerateTime; // 数据生成时间（14位）

    /**
     * 构造报文字符串
     */
    @Override
    public String buildMessage() {
        String header = String.format("%s;%s;%d;%s",
                getMineCode(), getMineName(), getMineType(), getUploadTime());
        String body = String.format("%s;%s;%s;%s;%s;%s;%s;%d;%d;%s;%s;%s;%s;%s;%s",
                workCode, designCode == null ? "" : designCode,
                workTypeCode, workPosition,
                holeNum == null ? "" : holeNum,
                workDate, workClass,
                drillingCount, retractCount,
                workMaster == null ? "" : workMaster,
                videoUrl, acceptor == null ? "" : acceptor,
                acceptConclusion == null ? "" : acceptConclusion,
                reporter, reportTime, dataGenerateTime);
        return header + "~" + body + "~||";
    }

}
