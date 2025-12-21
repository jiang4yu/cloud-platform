package com.maxinhai.platform.po;

import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 示例实体类：JSON记录
 * @Document：指定映射的MongoDB集合名（相当于MySQL的表名）
 */
@Data
@Document(collection = "t_json_record") // 集合名：t_json_record，不存在会自动创建
public class JsonRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    @Field("id")
    private String id;

    /**
     * 用户名 @Field：可选，指定映射的集合字段名，不指定则默认用属性名
     */
    @Field("key")
    private String key;

    /**
     * 字段
     */
    @Field("field")
    private String field;

    /**
     * 数值
     */
    @Field("value")
    private String value;

    /**
     * 数值数据类型
     */
    @Field("value_type")
    private String valueType;

    /**
     * 创建时间
     */
    @CreatedDate
    @Field("create_time")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @CreatedBy
    @Field("create_by")
    private String createBy;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Field("update_time")
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Field("update_by")
    private String updateBy;

}
