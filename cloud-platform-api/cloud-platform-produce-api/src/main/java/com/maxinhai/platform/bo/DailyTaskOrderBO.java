package com.maxinhai.platform.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "每日派工单完工数量VO")
public class DailyTaskOrderBO {

    @ApiModelProperty(value = "日期")
    private String daily;
    @ApiModelProperty(value = "工序编码")
    private String opCode;
    @ApiModelProperty(value = "派工单完工数量")
    private long finishQty;

}
