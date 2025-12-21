package com.maxinhai.platform.vo.worktime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "员工工时VO")
public class EmployeeWorkTimeVO {

    @ApiModelProperty(value = "用户ID")
    private String userId;
    @ApiModelProperty(value = "账号")
    private String account;
    @ApiModelProperty(value = "用户昵称")
    private String username;
    @ApiModelProperty(value = "工时")
    private long workTime;

}
