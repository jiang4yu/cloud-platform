package com.maxinhai.platform.vo.worktime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "车间工时VO")
public class WorkShopWorkTimeVO {

    @ApiModelProperty(value = "车间ID")
    private String workShopId;
    @ApiModelProperty(value = "车间名称")
    private String workShopName;
    @ApiModelProperty(value = "工时")
    private long workTime;

}
