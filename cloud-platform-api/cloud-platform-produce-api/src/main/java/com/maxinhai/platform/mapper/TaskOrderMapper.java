package com.maxinhai.platform.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.maxinhai.platform.bo.DailyProcessFinishTaskOrderQtyBO;
import com.maxinhai.platform.bo.DailyTaskOrderBO;
import com.maxinhai.platform.po.TaskOrder;
import com.maxinhai.platform.vo.worktime.CountTaskFinishQtyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
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
            "  limit 5000" +
            ") " +
            "and task.status in (0,1,2,3) " +
            "order by task.sort asc")
    List<TaskOrder> queryCanStartTaskList();

    @Select(value = "select task.work_order_id, task.id, task.sort, task.status " +
            "from prod_task_order task " +
            "where task.del_flag = 0 " +
            "and task.work_order_id in (" +
            "  select wo.id " +
            "  from prod_work_order wo " +
            "  where wo.del_flag = 0 " +
            "  and wo.order_status in (0,1,2,3) " +
            "  and wo.plan_begin_time between #{beginTime} and #{endTime} " +
            "  order by wo.order_id asc " +
            "  limit 1000" +
            ") " +
            "and task.status in (0,1,2,3) " +
            "order by task.sort asc")
    List<TaskOrder> queryCanStartedTaskList(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    /**
     * 查询每天每道工序派工单完成数量
     *
     * @return 每天每道工序派工单完成数量
     */
    @Select(value = "select date(task.actual_end_time) as daily, task.operation_id, op.code as operation_code, op.name as operation_name, count(*) as qty " +
            "from prod_task_order task " +
            "inner join mdm_operation op on task.operation_id = op.id " +
            "where task.del_flag = 0 and task.status = 4 " +
            "group by date(task.actual_end_time), task.operation_id, op.code, op.name " +
            "order by date(task.actual_end_time) ")
    List<DailyProcessFinishTaskOrderQtyBO> queryDailyProcessFinishTaskOrderQty();

    /**
     * 根据开始时间、结束时间统计每天派工单完工数量
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 每天派工单完工数量
     */
    @Select(value = "select date(actual_end_time) as daily, count(*) as finishQty " +
            "from prod_task_order " +
            "where del_flag = 0 " +
            "and actual_end_time between #{beginTime} and #{endTime} " +
            "group by date(actual_end_time) " +
            "order by date(actual_end_time) asc")
    List<DailyTaskOrderBO> queryDailyTaskOrder(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    @Select(value = "select date(task.actual_end_time) as daily, op.code as opCode, count(*) as finishQty " +
            "from prod_task_order task " +
            "inner join mdm_operation op on task.operation_id = op.id " +
            "where task.del_flag = 0 " +
            "and op.del_flag = 0 " +
            "and task.actual_end_time between #{beginTime} and #{endTime} " +
            "group by date(task.actual_end_time), op.code " +
            "order by date(task.actual_end_time) asc")
    List<DailyTaskOrderBO> queryDailyOpTaskOrder(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    @Select(value = "select wo.id as workOrderId, wo.work_order_code as workOrderCode, count(*) as finishQty " +
            "from prod_order so " +
            "inner join prod_work_order wo on wo.order_id = so.id " +
            "inner join prod_task_order task on task.order_id = so.id and task.work_order_id = wo.id and task.status = 4 " +
            "where task.del_flag = 0 " +
            "and wo.del_flag = 0 " +
            "and so.del_flag = 0 " +
            "group by wo.id, wo.work_order_code")
    List<CountTaskFinishQtyVO> countTaskFinishQtyByWorkOrderId();

    @Select(value = "select so.id as orderId, so.order_code as orderCode, count(*) as finishQty " +
            "from prod_order so " +
            "inner join prod_work_order wo on wo.order_id = so.id " +
            "inner join prod_task_order task on task.order_id = so.id and task.work_order_id = wo.id and task.status = 4 " +
            "where task.del_flag = 0 " +
            "and wo.del_flag = 0 " +
            "and so.del_flag = 0 " +
            "group by so.id, so.order_code")
    List<CountTaskFinishQtyVO> countTaskFinishQtyByOrderId();

}
