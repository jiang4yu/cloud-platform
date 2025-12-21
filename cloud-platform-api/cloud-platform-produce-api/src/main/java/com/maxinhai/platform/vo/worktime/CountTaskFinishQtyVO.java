package com.maxinhai.platform.vo.worktime;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "派工单完工数量VO")
public class CountTaskFinishQtyVO {

    private String orderId;
    private String orderCode;
    private String workOrderId;
    private String workOrderCode;
    private long finishQty;

}
