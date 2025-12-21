package com.maxinhai.platform.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.maxinhai.platform.po.OperateRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OperateRecordMapper extends MPJBaseMapper<OperateRecord> {

    @Select(value = "select record.task_order_id, record.operate_type, record.operate_time " +
            "from prod_operate_record record " +
            "inner join prod_task_order task on task.id = record.task_order_id " +
            "inner join prod_work_order wo on wo.id = task.work_order_id " +
            "where record.del_flag = 0 " +
            "and task.del_flag = 0 " +
            "and wo.del_flag = 0 " +
            "and wo.id = #{workOrderId} " +
            "order by record.operate_time asc")
    List<OperateRecord> getWorkOrderWorkTime(@Param("workOrderId") String workOrderId);

    @Select(value = "select record.task_order_id, record.operate_type, record.operate_time " +
            "from prod_operate_record record " +
            "inner join prod_task_order task on task.id = record.task_order_id " +
            "inner join prod_work_order wo on wo.id = task.work_order_id " +
            "inner join prod_order so on so.id = wo.order_id " +
            "where record.del_flag = 0 " +
            "and task.del_flag = 0 " +
            "and wo.del_flag = 0 " +
            "and so.del_flag = 0 " +
            "and so.id = #{orderId} " +
            "order by record.operate_time asc")
    List<OperateRecord> getOrderWorkTime(@Param("orderId") String orderId);

}
