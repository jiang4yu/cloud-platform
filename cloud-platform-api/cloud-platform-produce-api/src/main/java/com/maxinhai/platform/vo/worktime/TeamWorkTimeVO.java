package com.maxinhai.platform.vo.worktime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "班组工时VO")
public class TeamWorkTimeVO {

    @ApiModelProperty(value = "班组ID")
    private String teamId;
    @ApiModelProperty(value = "班组名称")
    private String teamName;
    @ApiModelProperty(value = "工时")
    private long workTime;

}
