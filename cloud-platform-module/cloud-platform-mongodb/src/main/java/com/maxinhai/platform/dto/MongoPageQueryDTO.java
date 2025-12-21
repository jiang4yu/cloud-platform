package com.maxinhai.platform.dto;

import lombok.Data;

import java.util.Map;

/**
 * 定义通用分页查询参数 DTO
 */
@Data
public class MongoPageQueryDTO {

    /**
     * 页码（前端传1开始，后端自动转0开始）
     */
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 10;

    /**
     * 排序字段（如createTime、age）
     */
    private String sortField = "createTime";

    /**
     * 排序方向：asc（升序）、desc（降序）
     */
    private String sortDirection = "desc";

    /**
     * 动态查询条件
     * key：字段名（如username、age）
     * value：条件值，支持规则：
     * - 普通值：等于查询（如"zhangsan" → username = zhangsan）
     * - 含%：模糊查询（如"%zhang%" → username like zhang）
     * - 数组：范围查询（如[18,30] → age >=18 and age <=30）
     */
    private Map<String, Object> conditions;

}
