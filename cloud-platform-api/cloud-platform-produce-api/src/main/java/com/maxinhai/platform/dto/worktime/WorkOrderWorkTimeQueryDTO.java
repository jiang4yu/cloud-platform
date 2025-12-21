package com.maxinhai.platform.dto.worktime;

import com.maxinhai.platform.dto.PageSearch;
import com.maxinhai.platform.vo.worktime.WorkOrderWorkTimeVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "工单工时分页查询DTO")
public class WorkOrderWorkTimeQueryDTO extends PageSearch<WorkOrderWorkTimeVO> {

    @ApiModelProperty(value = "工单ID")
    private String workOrderId;
    @ApiModelProperty(value = "工单编码")
    private String workOrderCode;

}
