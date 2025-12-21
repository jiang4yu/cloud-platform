package com.maxinhai.platform.controller;

import com.maxinhai.platform.annotation.ApiLog;
import com.maxinhai.platform.dto.OperateRecordQueryDTO;
import com.maxinhai.platform.service.OperateRecordService;
import com.maxinhai.platform.utils.AjaxResult;
import com.maxinhai.platform.utils.PageResult;
import com.maxinhai.platform.vo.OperateRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RefreshScope
@RestController
@RequestMapping("/operateRecord")
@Api(tags = "派工单操作记录管理接口")
public class OperateRecordController {

    @Resource
    private OperateRecordService operateRecordService;

    @PostMapping("/searchByPage")
    @ApiOperation(value = "分页查询派工单操作记录信息", notes = "根据查询条件分页查询派工单操作记录信息")
    public AjaxResult<PageResult<OperateRecordVO>> searchByPage(@RequestBody OperateRecordQueryDTO param) {
        return AjaxResult.success(PageResult.convert(operateRecordService.searchByPage(param)));
    }

    @GetMapping("/getInfo/{id}")
    @ApiOperation(value = "获取派工单操作记录信息", notes = "根据派工单操作记录ID获取详细信息")
    public AjaxResult<OperateRecordVO> getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(operateRecordService.getInfo(id));
    }

    @PostMapping("/removeOperateRecord")
    @ApiOperation(value = "删除派工单操作记录信息", notes = "根据派工单操作记录ID数组删除派工单操作记录信息")
    public AjaxResult<Void> removeOperateRecord(@RequestBody String[] ids) {
        operateRecordService.remove(ids);
        return AjaxResult.success();
    }

    @ApiLog("获取派工单操作记录")
    @GetMapping("/getOperateRecords/{taskOrderId}")
    @ApiOperation(value = "获取派工单操作记录", notes = "根据派工单ID获取操作记录")
    public AjaxResult<List<OperateRecordVO>> getOperateRecords(@PathVariable("taskOrderId") String taskOrderId) {
        return AjaxResult.success(operateRecordService.getOperateRecords(taskOrderId));
    }

}
