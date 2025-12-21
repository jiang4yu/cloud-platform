package com.maxinhai.platform.controller;

import com.maxinhai.platform.annotation.ApiLog;
import com.maxinhai.platform.dto.WorkOrderQueryDTO;
import com.maxinhai.platform.service.WorkOrderService;
import com.maxinhai.platform.vo.WorkOrderVO;
import com.maxinhai.platform.utils.AjaxResult;
import com.maxinhai.platform.utils.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RefreshScope
@RestController
@RequestMapping("/workOrder")
@Api(tags = "工单管理接口")
public class WorkOrderController {

    @Resource
    private WorkOrderService workOrderService;

    @PostMapping("/searchByPage")
    @ApiOperation(value = "分页查询工单信息", notes = "根据查询条件分页查询工单信息")
    public AjaxResult<PageResult<WorkOrderVO>> searchByPage(@RequestBody WorkOrderQueryDTO param) {
        return AjaxResult.success(PageResult.convert(workOrderService.searchByPage(param)));
    }

    @GetMapping("/getInfo/{id}")
    @ApiOperation(value = "获取工单信息", notes = "根据工单ID获取详细信息")
    public AjaxResult<WorkOrderVO> getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(workOrderService.getInfo(id));
    }

    @ApiLog("删除工单信息")
    @PostMapping("/removeWorkOrder")
    @ApiOperation(value = "删除工单信息", notes = "根据工单ID数组删除工单信息")
    public AjaxResult<Void> removeWorkOrder(@RequestBody String[] ids) {
        workOrderService.remove(ids);
        return AjaxResult.success();
    }

}
