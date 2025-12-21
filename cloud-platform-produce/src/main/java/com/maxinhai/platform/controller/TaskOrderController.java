package com.maxinhai.platform.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maxinhai.platform.annotation.ApiLog;
import com.maxinhai.platform.bo.DailyProcessFinishTaskOrderQtyBO;
import com.maxinhai.platform.dto.TaskOrderQueryDTO;
import com.maxinhai.platform.enums.OrderStatus;
import com.maxinhai.platform.po.TaskOrder;
import com.maxinhai.platform.service.TaskOrderService;
import com.maxinhai.platform.utils.AjaxResult;
import com.maxinhai.platform.utils.DateUtils;
import com.maxinhai.platform.utils.PageResult;
import com.maxinhai.platform.vo.DailyOpTaskOrderVO;
import com.maxinhai.platform.vo.DailyTaskOrderVO;
import com.maxinhai.platform.vo.TaskOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RefreshScope
@RestController
@RequestMapping("/taskOrder")
@Api(tags = "派工单管理接口")
public class TaskOrderController {

    @Resource
    private TaskOrderService taskOrderService;

    @PostMapping("/searchByPage")
    @ApiOperation(value = "分页查询派工单信息", notes = "根据查询条件分页查询派工单信息")
    public AjaxResult<PageResult<TaskOrderVO>> searchByPage(@RequestBody TaskOrderQueryDTO param) {
        return AjaxResult.success(PageResult.convert(taskOrderService.searchByPage(param)));
    }

    @GetMapping("/getInfo/{id}")
    @ApiOperation(value = "获取派工单信息", notes = "根据派工单ID获取详细信息")
    public AjaxResult<TaskOrderVO> getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(taskOrderService.getInfo(id));
    }

    @PostMapping("/removeTaskOrder")
    @ApiOperation(value = "删除派工单信息", notes = "根据派工单ID数组删除派工单信息")
    public AjaxResult<Void> removeTaskOrder(@RequestBody String[] ids) {
        taskOrderService.remove(ids);
        return AjaxResult.success();
    }

    @ApiLog("派工单开工")
    @GetMapping("/startWork/{taskOrderId}")
    @ApiOperation(value = "派工单开工", notes = "根据派工单ID开工")
    public AjaxResult<TaskOrderVO> startWork(@PathVariable("taskOrderId") String taskOrderId) {
        taskOrderService.startWork(taskOrderId);
        return AjaxResult.success();
    }

    @ApiLog("派工单暂停")
    @GetMapping("/pauseWork/{taskOrderId}")
    @ApiOperation(value = "派工单暂停", notes = "根据派工单ID暂停")
    public AjaxResult<TaskOrderVO> pauseWork(@PathVariable("taskOrderId") String taskOrderId) {
        taskOrderService.pauseWork(taskOrderId);
        return AjaxResult.success();
    }

    @ApiLog("派工单复工")
    @GetMapping("/resumeWork/{taskOrderId}")
    @ApiOperation(value = "派工单复工", notes = "根据派工单ID复工")
    public AjaxResult<TaskOrderVO> resumeWork(@PathVariable("taskOrderId") String taskOrderId) {
        taskOrderService.resumeWork(taskOrderId);
        return AjaxResult.success();
    }

    @ApiLog("派工单报工")
    @GetMapping("/reportWork/{taskOrderId}")
    @ApiOperation(value = "派工单报工", notes = "根据派工单ID报工")
    public AjaxResult<TaskOrderVO> reportWork(@PathVariable("taskOrderId") String taskOrderId) {
        taskOrderService.reportWork(taskOrderId);
        return AjaxResult.success();
    }

    @ApiLog("派工单完成情况甘特图")
    @GetMapping("/printGanttChart")
    @ApiOperation(value = "派工单完成情况甘特图", notes = "派工单完成情况甘特图")
    public AjaxResult<Void> printGanttChart() {
        List<TaskOrder> taskOrderList = taskOrderService.list(new LambdaQueryWrapper<TaskOrder>()
                .select(TaskOrder::getId, TaskOrder::getStatus, TaskOrder::getActualEndTime)
                .between(TaskOrder::getActualEndTime, DateUtils.getBeginTimeOfMonth(), DateUtils.getEndTimeOfMonth())
                .orderByAsc(TaskOrder::getActualEndTime));
        printGanttChart(taskOrderList);
        return AjaxResult.success();
    }

    @ApiLog("查询每天每道工序派工单完成数量")
    @GetMapping("/queryDailyProcessFinishTaskOrderQty")
    @ApiOperation(value = "查询每天每道工序派工单完成数量", notes = "查询每天每道工序派工单完成数量")
    public AjaxResult<List<DailyProcessFinishTaskOrderQtyBO>> queryDailyProcessFinishTaskOrderQty() {
        return AjaxResult.success(taskOrderService.queryDailyProcessFinishTaskOrderQty());
    }

    @ApiLog("统计当月派工单完工数量折线图")
    @GetMapping("/countDailyTaskOrder")
    @ApiOperation(value = "统计当月派工单完工数量折线图", notes = "统计当月派工单完工数量折线图")
    public AjaxResult<DailyTaskOrderVO> countDailyTaskOrder() {
        return AjaxResult.success(taskOrderService.countDailyTaskOrder());
    }

    private static boolean flag = true;

    @ApiLog("统计当月每日每道工序派工单完工数量折线图")
    @GetMapping("/countDailyOpTaskOrder")
    @ApiOperation(value = "统计当月每日每道工序派工单完工数量折线图", notes = "统计当月每日每道工序派工单完工数量折线图")
    public AjaxResult<DailyOpTaskOrderVO> countDailyOpTaskOrder() {
        DailyOpTaskOrderVO result = null;
        if (flag) {
            result = taskOrderService.countDailyOpTaskOrder();
        } else {
            result = taskOrderService.countDailyOpTaskOrderEx();
        }
        flag = !flag;
        return AjaxResult.success(result);
    }

    private static final int BAR_LENGTH = 50; // 最长条形图长度

    public static void printGanttChart(List<TaskOrder> taskOrders) {
        // 筛选已完工且有实际结束时间的派工单
        List<TaskOrder> completedOrders = taskOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.REPORT)
                .filter(order -> order.getActualEndTime() != null)
                .collect(Collectors.toList());

        if (completedOrders.isEmpty()) {
            log.info("没有已完工的派工单数据");
            return;
        }

        // 按日期分组统计数量
        Map<String, Long> dailyCount = completedOrders.stream()
                .collect(Collectors.groupingBy(
                        order -> DateUtil.format(order.getActualEndTime(), "yyyy-MM-dd"),
                        Collectors.counting()
                ));

        // 获取所有日期并排序
        List<String> sortedDates = new ArrayList<>(dailyCount.keySet());
        Collections.sort(sortedDates);

        // 找出最大数量用于计算比例
        long maxCount = dailyCount.values().stream().mapToLong(Long::longValue).max().orElse(1);

        // 打印表头
        log.info("派工单完工情况甘特图");
        log.info("日期\t\t\t完工数量\t分布");
        log.info("----------------------------------------");

        // 打印每一天的数据
        for (String date : sortedDates) {
            long count = dailyCount.get(date);
            // 计算条形图长度
            int bar = (int) (count * BAR_LENGTH / maxCount);

            // 构建条形图
            StringBuilder barStr = new StringBuilder();
            for (int i = 0; i < bar; i++) {
                barStr.append("■");
            }

            // 打印一行数据
            log.info("{}\t{}\t{}", date, count, barStr.toString());
        }
    }

}
