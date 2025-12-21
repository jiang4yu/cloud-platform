package com.maxinhai.platform.vo.worktime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "工单工时VO")
public class WorkOrderWorkTimeVO {

    @ApiModelProperty("工单ID")
    private String workOrderId;
    @ApiModelProperty("工单编码")
    private String workOrderCode;
    @ApiModelProperty("工时")
    private long workTime;

}
