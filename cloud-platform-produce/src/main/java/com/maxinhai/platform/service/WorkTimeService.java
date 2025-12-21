package com.maxinhai.platform.service;

import com.maxinhai.platform.vo.worktime.*;

import java.util.List;

public interface WorkTimeService {

    /**
     * 获取员工工时
     *
     * @return 员工工时
     */
    EmployeeWorkTimeVO getEmployeeWorkTime();

    /**
     * 获取班组工时
     *
     * @return 班组工时
     */
    TeamWorkTimeVO getTeamWorkTime();

    /**
     * 获取车间工时
     *
     * @return 车间工时
     */
    WorkShopWorkTimeVO getWorkShopWorkTime();

    /**
     * ]
     * 获取产线工时
     *
     * @return 产线工时
     */
    ProductionLineWorkTimeVO getProductionLineWorkTime();

    /**
     * 获取设备生产工时
     *
     * @return 设备生产工时
     */
    EquipWorkTimeVO getEquipWorkTime();

    /**
     * 获取工单工时
     *
     * @param workOrderId 工单ID
     * @return 工单工时
     */
    WorkOrderWorkTimeVO getWorkOrderWorkTime(String workOrderId);

    /**
     * 获取订单工时
     *
     * @param orderId 订单ID
     * @return 订单工时
     */
    OrderWorkTimeVO getOrderWorkTime(String orderId);

    /**
     * 根据工单维度统计派工单完成数
     *
     * @return 派工单完成数
     */
    List<CountTaskFinishQtyVO> countTaskFinishQtyByWorkOrderId();

    /**
     * 根据订单维度统计派工单完成数
     *
     * @return 派工单完成数
     */
    List<CountTaskFinishQtyVO> countTaskFinishQtyByOrderId();

}
