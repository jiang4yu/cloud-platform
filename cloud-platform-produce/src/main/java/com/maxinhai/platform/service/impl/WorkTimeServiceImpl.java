package com.maxinhai.platform.service.impl;

import com.maxinhai.platform.mapper.OperateRecordMapper;
import com.maxinhai.platform.mapper.TaskOrderMapper;
import com.maxinhai.platform.po.OperateRecord;
import com.maxinhai.platform.service.WorkTimeService;
import com.maxinhai.platform.utils.WorkTimeUtils;
import com.maxinhai.platform.vo.worktime.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkTimeServiceImpl implements WorkTimeService {

    private final OperateRecordMapper operateRecordMapper;
    private final TaskOrderMapper taskOrderMapper;

    @Override
    public EmployeeWorkTimeVO getEmployeeWorkTime() {
        return null;
    }

    @Override
    public TeamWorkTimeVO getTeamWorkTime() {
        return null;
    }

    @Override
    public WorkShopWorkTimeVO getWorkShopWorkTime() {
        return null;
    }

    @Override
    public ProductionLineWorkTimeVO getProductionLineWorkTime() {
        return null;
    }

    @Override
    public EquipWorkTimeVO getEquipWorkTime() {
        return null;
    }

    @Override
    public WorkOrderWorkTimeVO getWorkOrderWorkTime(String workOrderId) {
        List<OperateRecord> recordList = operateRecordMapper.getWorkOrderWorkTime(workOrderId);
        BigDecimal workHour = WorkTimeUtils.calculateWorkOrderWorkHour(recordList);
        return new WorkOrderWorkTimeVO(workOrderId, "", workHour.longValue());
    }

    @Override
    public OrderWorkTimeVO getOrderWorkTime(String orderId) {
        List<OperateRecord> recordList = operateRecordMapper.getOrderWorkTime(orderId);
        BigDecimal workHour = WorkTimeUtils.calculateOrderWorkHour(recordList);
        return new OrderWorkTimeVO(orderId, "", workHour.longValue());
    }

    @Override
    public List<CountTaskFinishQtyVO> countTaskFinishQtyByWorkOrderId() {
        List<CountTaskFinishQtyVO> taskFinishQtyVOList = taskOrderMapper.countTaskFinishQtyByWorkOrderId();
        return taskFinishQtyVOList.size() > 100 ? taskFinishQtyVOList.subList(0, 100) : taskFinishQtyVOList;
    }

    @Override
    public List<CountTaskFinishQtyVO> countTaskFinishQtyByOrderId() {
        List<CountTaskFinishQtyVO> taskFinishQtyVOList = taskOrderMapper.countTaskFinishQtyByOrderId();
        return taskFinishQtyVOList.size() > 100 ? taskFinishQtyVOList.subList(0, 100) : taskFinishQtyVOList;
    }
}
