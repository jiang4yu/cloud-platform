package com.maxinhai.platform.dto.worktime;

import com.maxinhai.platform.dto.PageSearch;
import com.maxinhai.platform.vo.worktime.OrderWorkTimeVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "订单工时分页查询DTO")
public class OrderWorkTimeQueryDTO extends PageSearch<OrderWorkTimeVO> {

    @ApiModelProperty(value = "订单ID")
    private String orderId;
    @ApiModelProperty(value = "订单编码")
    private String orderCode;

}
