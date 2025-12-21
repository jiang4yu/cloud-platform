package com.maxinhai.platform.builder;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 重构后的MongoDB查询条件构建器（支持链式调用、参数校验、条件组合）
 * 使用示例：
 * QueryBuilder.create()
 * .eq(user, "username", User::getUsername)
 * .gt(user, "age", User::getAge)
 * .like(user, "nickname", User::getNickname)
 * .in("role", List.of("admin", "user"))
 * .build();
 */
public class QueryBuilder {

    // 核心：保存当前构建的Criteria（支持链式组合）
    private Criteria currentCriteria;

    // 私有构造：通过静态方法创建实例
    private QueryBuilder() {
        this.currentCriteria = new Criteria();
    }

    // ==================== 基础条件方法（链式调用） ====================

    /**
     * 严格查询字段类型为 null 的文档（匹配字段存在且类型为null）
     * 注释与实现对齐：原注释写“严格类型null”，实现改为isNullValue()
     */
    public QueryBuilder isNull(String fieldName) {
        Assert.hasText(fieldName, "字段名fieldName不能为空！");
        this.currentCriteria = this.currentCriteria.and(fieldName).isNullValue();
        return this;
    }

    /**
     * 查询字段值等于指定值的文档（链式版）
     */
    public <T, R> QueryBuilder eq(T obj, String fieldName, Function<T, R> fieldValue) {
        return addCompareCondition(obj, fieldName, fieldValue, (c, v) -> c.is(v));
    }

    /**
     * 查询字段值不等于指定值的文档（链式版）
     */
    public <T, R> QueryBuilder ne(T obj, String fieldName, Function<T, R> fieldValue) {
        return addCompareCondition(obj, fieldName, fieldValue, (c, v) -> c.ne(v));
    }

    /**
     * 模糊查询（正则匹配：包含指定值）（链式版）
     */
    public <T, R> QueryBuilder like(T obj, String fieldName, Function<T, R> fieldValue) {
        Assert.hasText(fieldName, "字段名fieldName不能为空！");
        Assert.notNull(fieldValue, "字段值提取器fieldValue不能为空！");
        if (obj == null) {
            return this; // 空对象跳过该条件
        }

        R value = fieldValue.apply(obj);
        if (Objects.isNull(value)) {
            return this; // 空值跳过该条件（也可抛异常：Assert.notNull(value, "模糊查询值不能为空！")）
        }
        this.currentCriteria = this.currentCriteria.and(fieldName).regex(value.toString());
        return this;
    }

    /**
     * 小于（链式版）
     */
    public <T, R> QueryBuilder lt(T obj, String fieldName, Function<T, R> fieldValue) {
        return addCompareCondition(obj, fieldName, fieldValue, (c, v) -> c.lt(v));
    }

    /**
     * 小于等于（链式版）
     */
    public <T, R> QueryBuilder lte(T obj, String fieldName, Function<T, R> fieldValue) {
        return addCompareCondition(obj, fieldName, fieldValue, (c, v) -> c.lte(v));
    }

    /**
     * 大于（链式版）
     */
    public <T, R> QueryBuilder gt(T obj, String fieldName, Function<T, R> fieldValue) {
        return addCompareCondition(obj, fieldName, fieldValue, (c, v) -> c.gt(v));
    }

    /**
     * 大于等于（链式版）
     */
    public <T, R> QueryBuilder gte(T obj, String fieldName, Function<T, R> fieldValue) {
        return addCompareCondition(obj, fieldName, fieldValue, (c, v) -> c.gte(v));
    }

    /**
     * 范围查询（gte(min) and lte(max)）（链式版）
     */
    public <T, R> QueryBuilder between(T obj, String fieldName, Function<T, R> minValue, Function<T, R> maxValue) {
        Assert.hasText(fieldName, "字段名fieldName不能为空！");
        Assert.notNull(minValue, "最小值提取器minValue不能为空！");
        Assert.notNull(maxValue, "最大值提取器maxValue不能为空！");
        if (obj == null) {
            return this;
        }

        R min = minValue.apply(obj);
        R max = maxValue.apply(obj);
        this.currentCriteria = this.currentCriteria.and(fieldName).gte(min).lte(max);
        return this;
    }

    /**
     * in查询（常规场景：直接传值集合，推荐）（链式版）
     */
    public <R> QueryBuilder in(String fieldName, Collection<R> values) {
        Assert.hasText(fieldName, "字段名fieldName不能为空！");
        Assert.notEmpty(values, "in查询值集合不能为空！");
        this.currentCriteria = this.currentCriteria.and(fieldName).in(values);
        return this;
    }

    /**
     * in查询（特殊场景：通过Function提取值）（链式版）
     */
    public <T, R> QueryBuilder in(T obj, String fieldName, List<Function<T, R>> valueExtractors) {
        Assert.hasText(fieldName, "字段名fieldName不能为空！");
        Assert.notEmpty(valueExtractors, "in查询值提取器集合不能为空！");
        if (obj == null) {
            return this;
        }

        List<R> values = valueExtractors.stream()
                .map(extractor -> extractor.apply(obj))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!values.isEmpty()) {
            this.currentCriteria = this.currentCriteria.and(fieldName).in(values);
        }
        return this;
    }

    /**
     * notIn查询（常规场景：直接传值集合）（链式版）
     */
    public <R> QueryBuilder notIn(String fieldName, Collection<R> values) {
        Assert.hasText(fieldName, "字段名fieldName不能为空！");
        Assert.notEmpty(values, "notIn查询值集合不能为空！");
        this.currentCriteria = this.currentCriteria.and(fieldName).nin(values);
        return this;
    }

    /**
     * all查询（数组字段包含所有指定值）（链式版）
     */
    public <R> QueryBuilder all(String fieldName, Collection<R> values) {
        Assert.hasText(fieldName, "字段名fieldName不能为空！");
        Assert.notEmpty(values, "all查询值集合不能为空！");
        this.currentCriteria = this.currentCriteria.and(fieldName).all(values);
        return this;
    }

    // ==================== 条件组合方法（and/or） ====================

    /**
     * 组合多个Criteria（and关系）
     */
    public QueryBuilder and(Criteria... criteria) {
        Assert.notEmpty(criteria, "and组合的条件不能为空！");
        this.currentCriteria = this.currentCriteria.andOperator(criteria);
        return this;
    }

    /**
     * 组合多个Criteria（or关系）
     */
    public QueryBuilder or(Criteria... criteria) {
        Assert.notEmpty(criteria, "or组合的条件不能为空！");
        this.currentCriteria = this.currentCriteria.orOperator(criteria);
        return this;
    }

    // ==================== 辅助方法 ====================

    /**
     * 通用比较条件封装（减少重复代码）
     */
    private <T, R> QueryBuilder addCompareCondition(T obj, String fieldName, Function<T, R> fieldValue, CompareHandler handler) {
        Assert.hasText(fieldName, "字段名fieldName不能为空！");
        Assert.notNull(fieldValue, "字段值提取器fieldValue不能为空！");
        if (obj == null) {
            return this; // 空对象跳过该条件
        }

        R value = fieldValue.apply(obj);
        if (Objects.isNull(value)) {
            return this; // 空值跳过该条件（业务需要可改为抛异常）
        }
        // 调用具体的比较逻辑（is/ne/lt等）
        this.currentCriteria = handler.handle(this.currentCriteria.and(fieldName), value);
        return this;
    }

    /**
     * 构建最终的Criteria（链式调用结束）
     */
    public Criteria build() {
        return this.currentCriteria;
    }

    /**
     * 比较逻辑处理器（内部接口，封装is/ne/lt等逻辑）
     */
    @FunctionalInterface
    private interface CompareHandler {
        Criteria handle(Criteria criteria, Object value);
    }

    /**
     * 创建空的构建器（推荐：支持链式调用）
     */
    public static QueryBuilder create() {
        return new QueryBuilder();
    }

}
