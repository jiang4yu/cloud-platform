package com.maxinhai.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.maxinhai.platform.bo.DailyProcessFinishTaskOrderQtyBO;
import com.maxinhai.platform.bo.DailyTaskOrderBO;
import com.maxinhai.platform.dto.TaskOrderQueryDTO;
import com.maxinhai.platform.enums.OperateType;
import com.maxinhai.platform.enums.OrderStatus;
import com.maxinhai.platform.exception.BusinessException;
import com.maxinhai.platform.listener.CheckOrderEvent;
import com.maxinhai.platform.listener.OperationCheckOrderEvent;
import com.maxinhai.platform.mapper.OrderMapper;
import com.maxinhai.platform.mapper.TaskOrderMapper;
import com.maxinhai.platform.mapper.WorkOrderMapper;
import com.maxinhai.platform.po.Order;
import com.maxinhai.platform.po.Product;
import com.maxinhai.platform.po.TaskOrder;
import com.maxinhai.platform.po.WorkOrder;
import com.maxinhai.platform.po.technology.Bom;
import com.maxinhai.platform.po.technology.Operation;
import com.maxinhai.platform.po.technology.Routing;
import com.maxinhai.platform.service.OperateRecordService;
import com.maxinhai.platform.service.TaskOrderService;
import com.maxinhai.platform.utils.DateUtils;
import com.maxinhai.platform.vo.DailyOpTaskOrderVO;
import com.maxinhai.platform.vo.DailyTaskOrderVO;
import com.maxinhai.platform.vo.TaskOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskOrderServiceImpl extends ServiceImpl<TaskOrderMapper, TaskOrder> implements TaskOrderService {

    @Resource
    private TaskOrderMapper taskOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OperateRecordService operateRecordService;
    @Resource
    private ApplicationContext applicationContext;

    @Override
    public Page<TaskOrderVO> searchByPage(TaskOrderQueryDTO param) {
        return taskOrderMapper.selectJoinPage(param.getPage(), TaskOrderVO.class,
                new MPJLambdaWrapper<TaskOrder>()
                        .innerJoin(Product.class, Product::getId, Order::getProductId)
                        .innerJoin(Bom.class, Bom::getId, Order::getBomId)
                        .innerJoin(Routing.class, Routing::getId, Order::getRoutingId)
                        .innerJoin(Operation.class, Operation::getId, TaskOrder::getOperationId)
                        // 查询条件
                        .like(StrUtil.isNotBlank(param.getTaskOrderCode()), TaskOrder::getTaskOrderCode, param.getTaskOrderCode())
                        .eq(Objects.nonNull(param.getStatus()), TaskOrder::getStatus, param.getStatus())
                        // 字段映射
                        .selectAll(TaskOrder.class)
                        .selectAs(Product::getCode, TaskOrderVO::getProductCode)
                        .selectAs(Product::getName, TaskOrderVO::getProductName)
                        .selectAs(Bom::getCode, TaskOrderVO::getBomCode)
                        .selectAs(Bom::getName, TaskOrderVO::getBomName)
                        .selectAs(Routing::getCode, TaskOrderVO::getRoutingCode)
                        .selectAs(Routing::getName, TaskOrderVO::getRoutingName)
                        .selectAs(Operation::getCode, TaskOrderVO::getOperationCode)
                        .selectAs(Operation::getName, TaskOrderVO::getOperationName)
                        // 排序
                        .orderByDesc(TaskOrder::getCreateTime));
    }

    @Override
    public TaskOrderVO getInfo(String id) {
        return taskOrderMapper.selectJoinOne(TaskOrderVO.class, new MPJLambdaWrapper<TaskOrder>()
                .innerJoin(Product.class, Product::getId, Order::getProductId)
                .innerJoin(Bom.class, Bom::getId, Order::getBomId)
                .innerJoin(Routing.class, Routing::getId, Order::getRoutingId)
                .innerJoin(Operation.class, Operation::getId, TaskOrder::getOperationId)
                // 字段映射
                .selectAll(TaskOrder.class)
                .selectAs(Product::getCode, TaskOrderVO::getProductCode)
                .selectAs(Product::getName, TaskOrderVO::getProductName)
                .selectAs(Bom::getCode, TaskOrderVO::getBomCode)
                .selectAs(Bom::getName, TaskOrderVO::getBomName)
                .selectAs(Routing::getCode, TaskOrderVO::getRoutingCode)
                .selectAs(Routing::getName, TaskOrderVO::getRoutingName)
                // 查询条件
                .eq(TaskOrder::getId, id));
    }

    @Override
    public void remove(String[] ids) {
        taskOrderMapper.deleteBatchIds(Arrays.stream(ids).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startWork(String taskOrderId) {
        TaskOrder taskOrder = taskOrderMapper.selectById(taskOrderId);
        if (Objects.isNull(taskOrder)) {
            throw new BusinessException("派工单不存在！");
        }

        TaskOrder preTaskOrder = getPreTaskOrder(taskOrder.getWorkOrderId(), taskOrderId);
        if (Objects.nonNull(preTaskOrder) && !OrderStatus.REPORT.equals(preTaskOrder.getStatus())) {
            log.error("派工单【{}】开工失败，上道工序未报工！", taskOrder.getId());
            throw new BusinessException("派工单开工失败，上道工序未报工！");
        }

        if (!OrderStatus.INIT.equals(taskOrder.getStatus())) {
            StringBuilder buffer = new StringBuilder("派工单开工失败，");
            switch (taskOrder.getStatus()) {
                case START:
                    buffer.append("派工单已开工!派工单ID：").append(taskOrder.getId());
                    break;
                case PAUSE:
                    buffer.append("派工单已暂停!派工单ID：").append(taskOrder.getId());
                    break;
                case RESUME:
                    buffer.append("派工单已复工!派工单ID：").append(taskOrder.getId());
                    break;
                case REPORT:
                    buffer.append("派工单已报工!派工单ID：").append(taskOrder.getId());
                    break;
                default:
                    buffer.append("派工单未知状态!派工单ID：").append(taskOrder.getId());
                    break;
            }
            throw new BusinessException(buffer.toString());
        }
        // 更新派工单状态
        taskOrder.setStatus(OrderStatus.START);
        taskOrder.setActualBeginTime(new Date());
        taskOrderMapper.updateById(taskOrder);

        // 创建开工记录
        operateRecordService.createRecord(OperateType.START, taskOrder.getId());

        // 更新工单状态
        if (checkWorkOrderStart(taskOrder.getWorkOrderId())) {
            // 当实际开工时间为null时，设置工单实际开工时间
            workOrderMapper.update(new LambdaUpdateWrapper<WorkOrder>()
                    .set(WorkOrder::getOrderStatus, OrderStatus.START)
                    .set(WorkOrder::getActualBeginTime, taskOrder.getActualBeginTime())
                    .eq(WorkOrder::getId, taskOrder.getWorkOrderId())
                    .isNull(WorkOrder::getActualBeginTime));
        }

        // 更新订单状态
        if (checkOrderStart(taskOrder.getOrderId())) {
            // 当实际开工时间为null时，设置订单实际开工时间
            orderMapper.update(new LambdaUpdateWrapper<Order>()
                    .set(Order::getOrderStatus, OrderStatus.START)
                    .set(Order::getActualBeginTime, taskOrder.getActualBeginTime())
                    .eq(Order::getId, taskOrder.getOrderId())
                    .isNull(Order::getActualBeginTime));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseWork(String taskId) {
        TaskOrder taskOrder = taskOrderMapper.selectById(taskId);
        if (Objects.isNull(taskOrder)) {
            throw new BusinessException("派工单不存在！");
        }
        if (!OrderStatus.START.equals(taskOrder.getStatus())) {
            StringBuilder buffer = new StringBuilder("派工单暂停失败，");
            switch (taskOrder.getStatus()) {
                case INIT:
                    buffer.append("派工单未开工!派工单ID：").append(taskOrder.getId());
                    break;
                case PAUSE:
                    buffer.append("派工单已暂停!派工单ID：").append(taskOrder.getId());
                    break;
                case RESUME:
                    buffer.append("派工单已复工!派工单ID：").append(taskOrder.getId());
                    break;
                case REPORT:
                    buffer.append("派工单已报工!派工单ID：").append(taskOrder.getId());
                    break;
                default:
                    buffer.append("派工单未知状态!派工单ID：").append(taskOrder.getId());
                    break;
            }
            throw new BusinessException(buffer.toString());
        }
        // 更新派工单状态
        taskOrder.setStatus(OrderStatus.PAUSE);
        taskOrderMapper.updateById(taskOrder);

        // 更新工单状态
        WorkOrder workOrder = workOrderMapper.selectById(taskOrder.getWorkOrderId());
        workOrder.setOrderStatus(OrderStatus.PAUSE);
        workOrderMapper.updateById(workOrder);

        // 更新订单状态
        Order order = orderMapper.selectById(taskOrder.getOrderId());
        order.setOrderStatus(OrderStatus.PAUSE);
        orderMapper.updateById(order);

        // 创建暂停记录
        operateRecordService.createRecord(OperateType.PAUSE, taskOrder.getId());
    }

    @Override
    public void resumeWork(String taskId) {
        TaskOrder taskOrder = taskOrderMapper.selectById(taskId);
        if (Objects.isNull(taskOrder)) {
            throw new BusinessException("派工单不存在！");
        }
        if (!OrderStatus.PAUSE.equals(taskOrder.getStatus())) {
            StringBuilder buffer = new StringBuilder("派工单复工失败，");
            switch (taskOrder.getStatus()) {
                case INIT:
                    buffer.append("派工单未开工!派工单ID：").append(taskOrder.getId());
                    break;
                case START:
                    buffer.append("派工单已开工!派工单ID：").append(taskOrder.getId());
                    break;
                case RESUME:
                    buffer.append("派工单已复工!派工单ID：").append(taskOrder.getId());
                    break;
                case REPORT:
                    buffer.append("派工单已报工!派工单ID：").append(taskOrder.getId());
                    break;
                default:
                    buffer.append("派工单未知状态!派工单ID：").append(taskOrder.getId());
                    break;
            }
            throw new BusinessException(buffer.toString());
        }
        // 更新派工单状态
        taskOrder.setStatus(OrderStatus.START);
        taskOrderMapper.updateById(taskOrder);

        // 更新工单状态
        WorkOrder workOrder = workOrderMapper.selectById(taskOrder.getWorkOrderId());
        workOrder.setOrderStatus(OrderStatus.START);
        workOrderMapper.updateById(workOrder);

        // 更新订单状态
        Order order = orderMapper.selectById(taskOrder.getOrderId());
        order.setOrderStatus(OrderStatus.START);
        orderMapper.updateById(order);

        // 创建复工记录
        operateRecordService.createRecord(OperateType.RESUME, taskOrder.getId());
    }

    @Override
    public void reportWork(String taskId) {
        TaskOrder taskOrder = taskOrderMapper.selectById(taskId);
        if (Objects.isNull(taskOrder)) {
            throw new BusinessException("派工单不存在！");
        }
        if (!OrderStatus.START.equals(taskOrder.getStatus())) {
            StringBuilder buffer = new StringBuilder("派工单复工失败，");
            switch (taskOrder.getStatus()) {
                case INIT:
                    buffer.append("派工单未开工!派工单ID：").append(taskOrder.getId());
                    break;
                case PAUSE:
                    buffer.append("派工单已暂停!派工单ID：").append(taskOrder.getId());
                    break;
                case RESUME:
                    buffer.append("派工单已复工!派工单ID：").append(taskOrder.getId());
                    break;
                case REPORT:
                    buffer.append("派工单已报工!派工单ID：").append(taskOrder.getId());
                    break;
                default:
                    buffer.append("派工单未知状态!派工单ID：").append(taskOrder.getId());
                    break;
            }
            throw new BusinessException(buffer.toString());
        }
        // 更新派工单状态
        taskOrder.setStatus(OrderStatus.REPORT);
        taskOrder.setActualEndTime(new Date());
        taskOrderMapper.updateById(taskOrder);

        // 更新工单状态
        if (checkWorkOrderReport(taskOrder.getWorkOrderId())) {
            WorkOrder workOrder = workOrderMapper.selectById(taskOrder.getWorkOrderId());
            workOrder.setOrderStatus(OrderStatus.REPORT);
            workOrder.setActualEndTime(taskOrder.getActualEndTime());
            workOrderMapper.updateById(workOrder);
            // 工单完工，生成质检单
            applicationContext.publishEvent(new CheckOrderEvent(this, workOrder.getId()));
        }

        // 更新订单状态
        if (checkOrderReport(taskOrder.getOrderId())) {
            Order order = orderMapper.selectById(taskOrder.getOrderId());
            order.setOrderStatus(OrderStatus.REPORT);
            order.setActualEndTime(taskOrder.getActualEndTime());
            orderMapper.updateById(order);
        }

        // 创建报工记录
        operateRecordService.createRecord(OperateType.REPORT, taskOrder.getId());

        // 派工单完工，生成工序质检单
        applicationContext.publishEvent(new OperationCheckOrderEvent(this, taskOrder.getId()));
    }

    @Override
    public TaskOrder getPreTaskOrder(String workOrderId, String taskOrderId) {
        List<TaskOrder> taskOrderList = taskOrderMapper.selectList(new LambdaQueryWrapper<TaskOrder>()
                .select(TaskOrder::getId, TaskOrder::getStatus, TaskOrder::getSort)
                .eq(TaskOrder::getWorkOrderId, workOrderId)
                .orderByAsc(TaskOrder::getSort));
        int currentIndex = -1;
        for (int index = 0; index < taskOrderList.size(); index++) {
            TaskOrder taskOrder = taskOrderList.get(index);
            if (taskOrder.getId().equals(taskOrderId)) {
                currentIndex = index;
                break;
            }
        }
        if (currentIndex == -1) {
            throw new BusinessException("未找到当前派工单！");
        }

        int nextIndex = currentIndex - 1;
        if (nextIndex < 0) {
            // 当前派工单是第一道工序或者最后一道工序，返回当前派工单，或者返回空
            return null;
        }
        return taskOrderList.get(nextIndex);
    }

    @Override
    public TaskOrder getNextTaskOrder(String workOrderId, String taskOrderId) {
        List<TaskOrder> taskOrderList = taskOrderMapper.selectList(new LambdaQueryWrapper<TaskOrder>()
                .select(TaskOrder::getId, TaskOrder::getStatus, TaskOrder::getSort)
                .eq(TaskOrder::getWorkOrderId, workOrderId)
                .orderByAsc(TaskOrder::getSort));
        int currentIndex = -1;
        for (int index = 0; index < taskOrderList.size(); index++) {
            TaskOrder taskOrder = taskOrderList.get(index);
            if (taskOrder.getId().equals(taskOrderId)) {
                currentIndex = index;
                break;
            }
        }
        if (currentIndex == -1) {
            throw new BusinessException("未找到当前派工单！");
        }

        int nextIndex = currentIndex + 1;
        if (nextIndex >= taskOrderList.size()) {
            // 当前派工单是第一道工序或者最后一道工序，返回当前派工单，或者返回空
            return null;
        }
        return taskOrderList.get(nextIndex);
    }

    @Override
    public boolean checkOrderStart(String orderId) {
        List<WorkOrder> workOrderList = workOrderMapper.selectList(new LambdaQueryWrapper<WorkOrder>()
                .select(WorkOrder::getId, WorkOrder::getOrderId, WorkOrder::getOrderStatus)
                .eq(WorkOrder::getOrderStatus, OrderStatus.START)
                .eq(WorkOrder::getOrderId, orderId));
        return !workOrderList.isEmpty();
    }

    @Override
    public boolean checkOrderReport(String orderId) {
        List<WorkOrder> workOrderList = workOrderMapper.selectList(new LambdaQueryWrapper<WorkOrder>()
                .select(WorkOrder::getId, WorkOrder::getOrderId, WorkOrder::getOrderStatus)
                .eq(WorkOrder::getOrderId, orderId));
        long reportCount = workOrderList.stream()
                .filter(workOrder -> workOrder.getOrderStatus().equals(OrderStatus.REPORT))
                .count();
        return workOrderList.size() == reportCount;
    }

    @Override
    public boolean checkWorkOrderStart(String workOrderId) {
        List<TaskOrder> taskOrderList = taskOrderMapper.selectList(new LambdaQueryWrapper<TaskOrder>()
                .select(TaskOrder::getId, TaskOrder::getWorkOrderId, TaskOrder::getStatus)
                .eq(TaskOrder::getWorkOrderId, workOrderId)
                .eq(TaskOrder::getStatus, OrderStatus.START)
                .eq(TaskOrder::getSort, 1));
        return !taskOrderList.isEmpty();
    }

    @Override
    public boolean checkWorkOrderReport(String workOrderId) {
        List<TaskOrder> taskOrderList = taskOrderMapper.selectList(new LambdaQueryWrapper<TaskOrder>()
                .select(TaskOrder::getId, TaskOrder::getOrderId, TaskOrder::getStatus)
                .eq(TaskOrder::getWorkOrderId, workOrderId));
        long reportCount = taskOrderList.stream()
                .filter(taskOrder -> taskOrder.getStatus().equals(OrderStatus.REPORT))
                .count();
        return taskOrderList.size() == reportCount;
    }

    @Override
    public long getTodayFinishTaskOrderCount() {
        return taskOrderMapper.selectJoinCount(new MPJLambdaWrapper<TaskOrder>()
                .innerJoin(WorkOrder.class, WorkOrder::getId, TaskOrder::getWorkOrderId)
                .innerJoin(Order.class, Order::getId, TaskOrder::getOrderId)
                .eq(TaskOrder::getStatus, OrderStatus.REPORT)
                .between(TaskOrder::getActualEndTime, DateUtils.getBeginTimeOfToday(), DateUtils.getEndTimeOfToday()));
    }

    @Override
    public List<DailyProcessFinishTaskOrderQtyBO> queryDailyProcessFinishTaskOrderQty() {
        return taskOrderMapper.queryDailyProcessFinishTaskOrderQty();
    }

    @Override
    public DailyTaskOrderVO countDailyTaskOrder() {
        Date now = new Date();
        List<DailyTaskOrderBO> dailyTaskOrderBOList = taskOrderMapper.queryDailyTaskOrder(DateUtil.beginOfMonth(now), DateUtil.endOfMonth(now));
        DailyTaskOrderVO dailyTaskOrderVO = new DailyTaskOrderVO();
        List<String> xAxis = new ArrayList<>(dailyTaskOrderBOList.size());
        List<Long> yAxis = new ArrayList<>(dailyTaskOrderBOList.size());
        for (DailyTaskOrderBO dailyTaskOrderBO : dailyTaskOrderBOList) {
            xAxis.add(dailyTaskOrderBO.getDaily());
            yAxis.add(dailyTaskOrderBO.getFinishQty());
        }
        dailyTaskOrderVO.setXAxis(xAxis);
        dailyTaskOrderVO.setYAxis(yAxis);
        return dailyTaskOrderVO;
    }

    @Override
    public DailyOpTaskOrderVO countDailyOpTaskOrder() {
        Date now = new Date();
        List<DailyTaskOrderBO> dailyTaskOrderBOList = taskOrderMapper.queryDailyOpTaskOrder(DateUtil.beginOfMonth(now), DateUtil.endOfMonth(now));
        // 空列表快速返回，避免后续空指针
        if (CollectionUtils.isEmpty(dailyTaskOrderBOList)) {
            DailyOpTaskOrderVO emptyVO = new DailyOpTaskOrderVO();
            emptyVO.setXAxis(new ArrayList<>());
            emptyVO.setYAxis(new ArrayList<>());
            return emptyVO;
        }


        // 双层分组：Map<日期, Map<工序编码, 完工数量总和>>
        Map<String, Map<String, Long>> dailyOpTaskMap = dailyTaskOrderBOList.stream()
                .collect(Collectors.groupingBy(
                        DailyTaskOrderBO::getDaily,  // 第一层：按日期分组
                        TreeMap::new,                // 关键：指定外层Map为TreeMap（天然按Key升序）
                        Collectors.groupingBy(
                                DailyTaskOrderBO::getOpCode,  // 第二层：按工序编码分组
                                Collectors.summingLong(DailyTaskOrderBO::getFinishQty)
                        )
                ));

        // 查找最大工序个数
        TreeSet<String> allOpCodes = dailyOpTaskMap.values().stream()
                .flatMap(opTaskMap -> opTaskMap.keySet().stream())
                .collect(Collectors.toCollection(TreeSet::new));
        Map<String, Integer> opCodeIndex = new HashMap<>(); // 核心优化：O(1)查找工序索引
        int idx = 0;
        for (String op : allOpCodes) opCodeIndex.put(op, idx++);
        int opCount = allOpCodes.size();

        // 初始化轴
        DailyOpTaskOrderVO dailyTaskOrderVO = new DailyOpTaskOrderVO();
        List<String> xAxis = new ArrayList<>(dailyOpTaskMap.keySet());
        List<List<Long>> yAxis = new ArrayList<>();
        for (int i = 0; i < dailyOpTaskMap.keySet().size(); i++) {
            yAxis.add(new ArrayList<>(Collections.nCopies(opCount, 0L)));
        }

        // 填充数据（简化遍历，去掉indexOf）
        for (int dateIdx = 0; dateIdx < xAxis.size(); dateIdx++) {
            String daily = xAxis.get(dateIdx);
            Map<String, Long> opTaskMap = dailyOpTaskMap.get(daily);
            if (opTaskMap == null) continue;
            // 遍历工序，填充数值（空值补0）
            for (Map.Entry<String, Long> entry : opTaskMap.entrySet()) {
                Integer opIdx = opCodeIndex.get(entry.getKey());
                if (opIdx != null) {
                    yAxis.get(dateIdx).set(opIdx, Optional.ofNullable(entry.getValue()).orElse(0L));
                }
            }
        }

        dailyTaskOrderVO.setXAxis(xAxis);
        dailyTaskOrderVO.setYAxis(yAxis);
        return dailyTaskOrderVO;
    }

    public DailyOpTaskOrderVO countDailyOpTaskOrderEx() {
        // ========== 1. 基础数据获取 & 空值防御 ==========
        Date now = new Date();
        List<DailyTaskOrderBO> dailyTaskOrderBOList = taskOrderMapper.queryDailyOpTaskOrder(
                DateUtil.beginOfMonth(now),
                DateUtil.endOfMonth(now)
        );
        if (CollectionUtils.isEmpty(dailyTaskOrderBOList)) {
            DailyOpTaskOrderVO emptyVO = new DailyOpTaskOrderVO();
            emptyVO.setXAxis(new ArrayList<>());
            emptyVO.setYAxis(new ArrayList<>());
            return emptyVO;
        }

        // ========== 2. 双层分组（日期升序，避免无序） ==========
        // 外层：日期(String) → 内层：工序编码(String) → 完工数量(Long)
        Map<String, Map<String, Long>> dailyOpTaskMap = dailyTaskOrderBOList.stream()
                .collect(Collectors.groupingBy(
                        DailyTaskOrderBO::getDaily,
                        TreeMap::new,  // 日期天然按yyyy-MM-dd升序，无需额外排序
                        Collectors.groupingBy(
                                DailyTaskOrderBO::getOpCode,
                                Collectors.summingLong(DailyTaskOrderBO::getFinishQty)
                        )
                ));

        // ========== 3. 提取核心元数据（避免重复计算） ==========
        // 3.1 日期轴（X轴）：直接取TreeMap的Key，天然升序，无需手动排序
        List<String> xAxis = new ArrayList<>(dailyOpTaskMap.keySet());
        int dateCount = xAxis.size(); // 日期总数

        // 3.2 全局工序编码（去重+升序）+ 工序索引映射（消除indexOf性能损耗）
        TreeSet<String> allOpCodes = dailyOpTaskMap.values().stream()
                .flatMap(opTaskMap -> opTaskMap.keySet().stream())
                .collect(Collectors.toCollection(TreeSet::new));
        int opCount = allOpCodes.size(); // 工序总数（替代原max）

        // 预存「工序编码→索引」映射，O(1)查找，替代多次indexOf(O(n))
        Map<String, Integer> opCode2Index = new HashMap<>(opCount);
        int idx = 0;
        for (String opCode : allOpCodes) {
            opCode2Index.put(opCode, idx++);
        }

        // ========== 4. 初始化Y轴（按「日期维度」组织，每个日期对应所有工序的数量） ==========
        // Y轴结构：子列表数=日期数，每个子列表长度=工序数，初始值0L（无数据补0）
        List<List<Long>> yAxis = new ArrayList<>(dateCount);
        for (int i = 0; i < dateCount; i++) {
            // 初始化子列表：长度=工序数，所有元素默认0L，避免后续空值
            List<Long> dateQtyList = new ArrayList<>(Collections.nCopies(opCount, 0L));
            yAxis.add(dateQtyList);
        }

        // ========== 5. 填充Y轴数据（高性能，无冗余查找） ==========
        for (int dateIndex = 0; dateIndex < dateCount; dateIndex++) {
            String daily = xAxis.get(dateIndex); // 当前日期（直接按索引取，无indexOf）
            Map<String, Long> opTaskMap = dailyOpTaskMap.get(daily);
            if (CollectionUtils.isEmpty(opTaskMap)) {
                continue; // 当日无数据，保持0L
            }

            // 遍历当日工序，填充对应数量
            for (Map.Entry<String, Long> opEntry : opTaskMap.entrySet()) {
                String opCode = opEntry.getKey();
                // 防御性处理：避免工序编码不在全局列表中（理论上不会出现）
                Integer opIndex = opCode2Index.get(opCode);
                if (opIndex == null) {
                    continue;
                }
                // 空值补0，避免null（summingLong理论不会返回null，防御性处理）
                Long finishQty = Optional.ofNullable(opEntry.getValue()).orElse(0L);
                // 填充数据：日期索引 + 工序索引 定位值
                yAxis.get(dateIndex).set(opIndex, finishQty);
            }
        }

        // ========== 6. 封装返回VO ==========
        DailyOpTaskOrderVO dailyTaskOrderVO = new DailyOpTaskOrderVO();
        dailyTaskOrderVO.setXAxis(xAxis);
        dailyTaskOrderVO.setYAxis(yAxis);
        return dailyTaskOrderVO;
    }

}
