package com.maxinhai.platform.utils;

import com.maxinhai.platform.enums.OperateType;
import com.maxinhai.platform.po.OperateRecord;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 工时工具类
 */
public class WorkTimeUtils {

    /**
     * 计算单个派工单的工时（小时，保留2位小数）
     *
     * @param records 派工单操作记录
     * @return 工时
     */
    public static BigDecimal calculateTaskOrderWorkHour(List<OperateRecord> records) {
        BigDecimal totalHours = BigDecimal.ZERO;
        Date workStartTime = null;

        // 按操作时间排序（确保计算逻辑正确）
        records.sort(Comparator.comparing(OperateRecord::getOperateTime));

        for (OperateRecord record : records) {
            OperateType type = record.getOperateType();
            Date operateTime = record.getOperateTime();

            if (type == OperateType.START || type == OperateType.RESUME) {
                workStartTime = operateTime;
            } else if ((type == OperateType.PAUSE || type == OperateType.REPORT) && workStartTime != null) {
                // 计算时间差（秒）
                long seconds = (operateTime.getTime() - workStartTime.getTime()) / 1000;
                BigDecimal hours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 4, BigDecimal.ROUND_HALF_UP);
                totalHours = totalHours.add(hours);
                workStartTime = null;
            }
        }

        // 处理未完成工单：开工/复工后未暂停/报工，计算到当前时间
        if (workStartTime != null) {
            long seconds = (new Date().getTime() - workStartTime.getTime()) / 1000;
            BigDecimal hours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 4, BigDecimal.ROUND_HALF_UP);
            totalHours = totalHours.add(hours);
        }

        return totalHours.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算单个工单的工时（小时，保留2位小数）
     *
     * @param records 派工单操作记录
     * @return 工时
     */
    public static BigDecimal calculateWorkOrderWorkHour(List<OperateRecord> records) {
        // 步骤1：按派工单ID分组（串行分组，分组是IO轻量操作）
        Map<String, List<OperateRecord>> taskGroup = records.stream()
                .collect(Collectors.groupingBy(OperateRecord::getTaskOrderId));

        // 步骤2：并行计算每个派工单的工时（核心并行逻辑）
        // 使用ConcurrentHashMap保证线程安全（并行写入）
        Map<String, BigDecimal> taskWorkHourMap = new ConcurrentHashMap<>();
        taskGroup.entrySet().parallelStream().forEach(entry -> {
            String taskOrderId = entry.getKey();
            List<OperateRecord> recordList = entry.getValue();
            BigDecimal workHour = calculateTaskOrderWorkHour(recordList);
            taskWorkHourMap.put(taskOrderId, workHour);
        });

        // 步骤3：汇总所有派工单的工时（串行汇总，轻量操作）
        return taskWorkHourMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算单个订单的工时（小时，保留2位小数）
     *
     * @param records 派工单操作记录
     * @return 工时
     */
    public static BigDecimal calculateOrderWorkHour(List<OperateRecord> records) {
        // 步骤1：按派工单ID分组（串行分组，分组是IO轻量操作）
        Map<String, List<OperateRecord>> taskGroup = records.stream()
                .collect(Collectors.groupingBy(OperateRecord::getTaskOrderId));

        // 步骤2：并行计算每个派工单的工时（核心并行逻辑）
        // 使用ConcurrentHashMap保证线程安全（并行写入）
        Map<String, BigDecimal> taskWorkHourMap = new ConcurrentHashMap<>();
        taskGroup.entrySet().parallelStream().forEach(entry -> {
            String taskOrderId = entry.getKey();
            List<OperateRecord> recordList = entry.getValue();
            BigDecimal workHour = calculateTaskOrderWorkHour(recordList);
            taskWorkHourMap.put(taskOrderId, workHour);
        });

        // 步骤3：汇总所有派工单的工时（串行汇总，轻量操作）
        return taskWorkHourMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
