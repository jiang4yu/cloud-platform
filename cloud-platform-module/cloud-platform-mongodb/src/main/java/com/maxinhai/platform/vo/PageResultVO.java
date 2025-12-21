package com.maxinhai.platform.vo;


import lombok.Data;

import java.util.List;

/**
 * 通用分页结果
 * @param <T> 数据类型
 */
@Data
public class PageResultVO<T> {

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 页大小
     */
    private Integer pageSize;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 构建分页结果
     */
    public static <T> PageResultVO<T> build(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        // 计算总页数
        int pages = total.intValue() % pageSize == 0 ? total.intValue() / pageSize : total.intValue() / pageSize + 1;
        result.setPages(pages);
        // 判断是否有下一页
        result.setHasNext(pageNum < pages);
        return result;
    }

}
