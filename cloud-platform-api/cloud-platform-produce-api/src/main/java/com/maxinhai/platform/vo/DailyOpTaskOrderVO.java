package com.maxinhai.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "每日工序派工单完工数量VO")
public class DailyOpTaskOrderVO {

    @ApiModelProperty(value = "X轴-日期")
    private List<String> xAxis;
    @ApiModelProperty(value = "Y轴-当日工序派工单完成数量")
    private List<List<Long>> yAxis;

}
