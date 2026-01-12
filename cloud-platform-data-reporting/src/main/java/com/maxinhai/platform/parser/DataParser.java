package com.maxinhai.platform.parser;

/**
 * 通用数据解析器接口
 * @param <T>
 */
public interface DataParser<T> {

    String parse(T data);

}
