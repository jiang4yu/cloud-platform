package com.maxinhai.platform.bo;

import com.maxinhai.platform.enums.ApiParamType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiParamBO {

    /**
     * 参数类型（FILE.文件 STRING.字符串 NUMBER.数值 BOOLEAN.布尔值）
     */
    private ApiParamType paramType;
    /**
     * 参数名称
     */
    private String name;
    /**
     * 参数值
     */
    private Object value;

}
