package com.maxinhai.platform.controller;

import com.maxinhai.platform.annotation.ApiLog;
import com.maxinhai.platform.dto.OrderAddDTO;
import com.maxinhai.platform.dto.OrderQueryDTO;
import com.maxinhai.platform.service.OrderService;
import com.maxinhai.platform.service.TaskOrderService;
import com.maxinhai.platform.service.WorkOrderService;
import com.maxinhai.platform.utils.AjaxResult;
import com.maxinhai.platform.utils.PageResult;
import com.maxinhai.platform.vo.OrderProgressVO;
import com.maxinhai.platform.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RefreshScope
@RestController
@RequestMapping("/order")
@Api(tags = "订单管理接口")
public class OrderController {

    @Resource
    private OrderService orderService;
    @Resource
    private WorkOrderService workOrderService;
    @Resource
    private TaskOrderService taskOrderService;

    @PostMapping("/searchByPage")
    @ApiOperation(value = "分页查询订单信息", notes = "根据查询条件分页查询订单信息")
    public AjaxResult<PageResult<OrderVO>> searchByPage(@RequestBody OrderQueryDTO param) {
        return AjaxResult.success(PageResult.convert(orderService.searchByPage(param)));
    }

    @GetMapping("/getInfo/{id}")
    @ApiOperation(value = "获取订单信息", notes = "根据订单ID获取详细信息")
    public AjaxResult<OrderVO> getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(orderService.getInfo(id));
    }

    @PostMapping("/removeOrder")
    @ApiOperation(value = "删除订单信息", notes = "根据订单ID数组删除订单信息")
    public AjaxResult<Void> removeOrder(@RequestBody String[] ids) {
        orderService.remove(ids);
        return AjaxResult.success();
    }

    @PostMapping("/addOrder")
    @ApiOperation(value = "创建订单", notes = "创建订单")
    public AjaxResult<Void> addOrder(@RequestBody OrderAddDTO param) {
        orderService.add(param);
        return AjaxResult.success("创建成功!");
    }

    @ApiLog("获取今日订单完成情况")
    @PostMapping("/getTodayFinishOrderInfo")
    @ApiOperation(value = "获取今日订单完成情况", notes = "获取今日订单完成情况")
    public AjaxResult<Map<String, Long>> getTodayFinishOrderInfo() {
        Map<String, Long> result = new HashMap<>();
        result.put("todayFinishOrderCount", orderService.getTodayFinishOrderCount());
        result.put("todayFinishWorkOrderCount", workOrderService.getTodayFinishWorkOrderCount());
        result.put("todayFinishTaskOrderCount", taskOrderService.getTodayFinishTaskOrderCount());
        return AjaxResult.success(result);
    }

    private boolean apiSwitch = true;

    @ApiLog("订单进度统计")
    @PostMapping("/orderProgress")
    @ApiOperation(value = "订单进度统计", notes = "订单进度统计")
    public AjaxResult<OrderProgressVO> orderProgress() {
        apiSwitch = !apiSwitch;
        if(apiSwitch) {
            return AjaxResult.success(orderService.orderProgress());
        } else {
            return AjaxResult.success(orderService.orderProgressEx());
        }
    }

    @ApiLog("订单进度统计(新)")
    @PostMapping("/orderProgressNew")
    @ApiOperation(value = "订单进度统计(新)", notes = "订单进度统计（效率高，减少数据库交互次数）")
    public AjaxResult<OrderProgressVO> orderProgressNew() {
        return AjaxResult.success(orderService.orderProgressEx1());
    }

    @ApiLog("订单进度统计(新1)")
    @PostMapping("/orderProgressNew1")
    @ApiOperation(value = "订单进度统计(新1)", notes = "订单进度统计（效率高，减少数据库交互次数，减少查询时数据）")
    public AjaxResult<OrderProgressVO> orderProgressNew1() {
        return AjaxResult.success(orderService.orderProgressEx2());
    }

    @ApiLog("订单进度统计(新2)")
    @PostMapping("/orderProgressNew2")
    @ApiOperation(value = "订单进度统计(新2)", notes = "订单进度统计（效率高，减少数据库交互次数，减少查询时数据，多线程异步查询，增加数据库瞬时压力）")
    public AjaxResult<OrderProgressVO> orderProgressNew2() throws ExecutionException, InterruptedException {
        return AjaxResult.success(orderService.orderProgressEx3());
    }

}
