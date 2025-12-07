package com.maxinhai.platform.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.maxinhai.platform.bo.DailyProcessFinishTaskOrderQtyBO;
import com.maxinhai.platform.po.TaskOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskOrderMapper extends MPJBaseMapper<TaskOrder> {

    @Select(value = "select o.id, o.order_code, o.actual_begin_time as order_begin_time, o.actual_end_time as order_end_time, " +
            "wo.work_order_code, wo.actual_begin_time as work_begin_time, wo.actual_end_time as work_end_time, " +
            "task.task_order_code, task.actual_begin_time as task_begin_time, task.actual_end_time as task_end_time " +
            "from prod_order o " +
            "inner join prod_work_order wo on o.id = wo.order_id " +
            "inner join prod_task_order task on task.work_order_id = wo.id " +
            "where o.del_flag = 0 " +
            "and wo.del_flag = 0 " +
            "and task.del_flag = 0 " +
            "and task.status = 4 " +
            "and wo.order_status = 4 " +
            "and o.order_status = 4")
    List<Object> selectTaskOrderList(@Param("taskOrder") String taskOrderCode);

    @Select(value = "select task.work_order_id, task.id, task.sort, task.status " +
            "from prod_task_order task " +
            "where task.del_flag = 0 " +
            "and task.work_order_id in (" +
            "  select wo.id " +
            "  from prod_work_order wo " +
            "  where wo.del_flag = 0 " +
            "  and wo.order_status in (0,1,2,3) " +
            "  order by wo.order_id asc " +
            "  limit 1000" +
            ") " +
            "and task.status in (0,1,2,3) " +
            "order by task.sort asc")
    List<TaskOrder> queryCanStartTaskList();

    /**
     * 查询每天每道工序派工单完成数量
     * @return 每天每道工序派工单完成数量
     */
    @Select(value = "select date(task.actual_end_time) as daily, task.operation_id, op.code as operation_code, op.name as operation_name, count(*) as qty " +
            "from prod_task_order task " +
            "inner join mdm_operation op on task.operation_id = op.id " +
            "where task.del_flag = 0 and task.status = 4 " +
            "group by date(task.actual_end_time), task.operation_id, op.code, op.name " +
            "order by date(task.actual_end_time) ")
    List<DailyProcessFinishTaskOrderQtyBO> queryDailyProcessFinishTaskOrderQty();

}
