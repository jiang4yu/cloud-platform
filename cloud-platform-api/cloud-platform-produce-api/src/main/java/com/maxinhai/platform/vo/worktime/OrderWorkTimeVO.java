package com.maxinhai.platform.vo.worktime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "订单工时VO")
public class OrderWorkTimeVO {

    @ApiModelProperty("订单ID")
    private String orderId;
    @ApiModelProperty("订单编码")
    private String orderCode;
    @ApiModelProperty("工时")
    private long workTime;


}
