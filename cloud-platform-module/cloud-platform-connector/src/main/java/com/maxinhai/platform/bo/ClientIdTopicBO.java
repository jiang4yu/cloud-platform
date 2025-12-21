package com.maxinhai.platform.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 内部类：存储客户端ID和Topic
 */
@Data
@AllArgsConstructor
public class ClientIdTopicBO {

    private final String clientId;
    private final String topic;

}
