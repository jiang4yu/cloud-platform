package com.maxinhai.platform.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Lists;
import com.maxinhai.platform.annotation.MqSubscribe;
import com.maxinhai.platform.annotation.MqttSubscribe;
import com.maxinhai.platform.bo.CheckResult;
import com.maxinhai.platform.builder.QueryBuilder;
import com.maxinhai.platform.dto.MongoPageQueryDTO;
import com.maxinhai.platform.dto.RealTimeCheckDTO;
import com.maxinhai.platform.handler.MqHandler;
import com.maxinhai.platform.handler.MqttHandler;
import com.maxinhai.platform.po.JsonRecord;
import com.maxinhai.platform.service.RealTimeCheckService;
import com.maxinhai.platform.service.RetryCallApiService;
import com.maxinhai.platform.utils.AjaxResult;
import com.maxinhai.platform.utils.MongoDbUtils;
import com.maxinhai.platform.utils.RabbitMQUtils;
import com.maxinhai.platform.vo.PageResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.amqp.core.Message;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/algorithm")
@Api(tags = "设备管理接口")
@RequiredArgsConstructor
public class RealTimeCheckController {

    private final RealTimeCheckService realTimeCheckService;
    private final RetryCallApiService retryCallApi;

    @PostMapping(value = "realTimeCheck")
    @ApiOperation(value = "算法实时检测", notes = "根据图片算法实时检测")
    public AjaxResult<CheckResult> realTimeCheck(@RequestBody RealTimeCheckDTO param) {
        return AjaxResult.success(realTimeCheckService.realTimeCheck(param));
    }

    @PostMapping(value = "callRealTimeCheck")
    @ApiOperation(value = "调用实时检测接口", notes = "调用实时检测接口")
    public AjaxResult<Void> callRealTimeCheck() {
        realTimeCheckService.callRealTimeCheck();
        return AjaxResult.success();
    }

    @PostMapping(value = "retryCallApi")
    @ApiOperation(value = "重试调用API", notes = "重试调用API")
    public AjaxResult<Void> retryCallApi() {
        String apiUrl = "http://localhost:10090/algorithm/realTimeCheck";
        RealTimeCheckDTO param = new RealTimeCheckDTO();
        param.setImages(Lists.newArrayList("1.jpg", "2.jpg", "3.jpg"));
        retryCallApi.retryCallApi(apiUrl, param);
        return AjaxResult.success();
    }

    @PostMapping(value = "pageQuery")
    @ApiOperation(value = "MongoDB分页查询", notes = "MongoDB分页查询")
    public AjaxResult<PageResultVO<JsonRecord>> pageQuery() {
        // 示例：分页查询用户（年龄18~30，用户名含zhang，第1页，每页10条）
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("key", "key_88");

        MongoPageQueryDTO pageQueryDTO = new MongoPageQueryDTO();
        pageQueryDTO.setPageNum(1);
        pageQueryDTO.setPageSize(5);
        pageQueryDTO.setConditions(conditions);
        PageResultVO<JsonRecord> pageResultVO = MongoDbUtils.pageQuery(pageQueryDTO, JsonRecord.class, "t_json_record");
        return AjaxResult.success(pageResultVO);
    }

    public AjaxResult<PageResultVO<JsonRecord>> pageQuery1() {
        JsonRecord jsonRecord = new JsonRecord();
        jsonRecord.setId("");
        jsonRecord.setKey("");
        jsonRecord.setField("");
        jsonRecord.setValue("");
        jsonRecord.setValueType("");
        DateTime createTime = DateUtil.parse("2025-12-07 19:30:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        jsonRecord.setCreateTime(createTime.toLocalDateTime());
        DateTime updateTime = DateUtil.parse("2025-12-07 20:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        jsonRecord.setUpdateTime(updateTime.toLocalDateTime());

        Criteria criteria = QueryBuilder.create()
                .between(jsonRecord, "create_time",
                        JsonRecord::getCreateTime, JsonRecord::getUpdateTime)
                .in("key", List.of("key_1", "key_2", "key_3"))
                .build();
        return null;
    }

    @Resource
    private MqttHandler mqttHandler;
    @Resource
    private MqHandler mqHandler;

    @Scheduled(initialDelay = 3000, fixedDelay = 3000)
    public void sendMqttMsg() throws MqttException {
//        mqttHandler.sendMessage("emqx", "/topic/mqtt_test", String.format("客户端：【%s】，消息：Hello Docker MQTT!!!", "emqx"));
//        mqttHandler.sendMessage("docker_emqx", "/topic/mqtt_test", String.format("客户端：【%s】，消息：Hello Docker MQTT!!!", "docker_emqx"));
//        mqttHandler.sendMessage("192.168.1.12_emqx", "/topic/mqtt_test", String.format("客户端：【%s】，消息：Hello Docker MQTT!!!", "192.168.1.12_emqx"));

        mqHandler.sendMessage("mq", "default.exchange:algorithm_control", "算法控制!!!");
        mqHandler.sendMessage("mq", "default.exchange:algorithm_status", "算法状态!!!");
        mqHandler.sendMessage("mq", "/rabbitmq/test", "Hello RabbitMQ!!!");
        mqHandler.sendMessage("mq", RabbitMQUtils.DEFAULT_EXCHANGE, "/maxinhai", "Hello RabbitMQ!!!");
    }

    /**
     * 接收Topic为"mqtt_test"的消息
     */
    @MqttSubscribe(clientId = "emqx", topic = "/topic/mqtt_test")
    public void receiveMqttMsg(MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("收到MQTT消息（emqx）：" + payload + "，QoS：" + message.getQos());
    }

    @MqttSubscribe(clientId = "docker_emqx", topic = "/topic/mqtt_test")
    public void receiveMqttMsg1(MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("收到MQTT消息（docker_emqx）：" + payload + "，QoS：" + message.getQos());
    }

    @MqttSubscribe(clientId = "192.168.1.12_emqx", topic = "/topic/mqtt_test")
    public void receiveMqttMsg2(MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("收到MQTT消息（192.168.1.12_emqx）：" + payload + "，QoS：" + message.getQos());
    }

    /**
     * 监听用户消息（自定义对象参数）
     */
    @MqSubscribe(clientId = "mq", routingKey = "/rabbitmq/test")
    public void receiveMqMsg(Message message) {
        System.out.println("【RabbitMQ消息】：" + new String(message.getBody(), StandardCharsets.UTF_8));
    }

    /**
     * 监听用户消息（自定义对象参数）
     */
    @MqSubscribe(clientId = "mq", routingKey = "default.exchange:algorithm_control")
    public void receiveMqMsg1(Message message) {
        System.out.println("【算法控制消息】：" + new String(message.getBody(), StandardCharsets.UTF_8));
    }

    /**
     * 监听用户消息（自定义对象参数）
     */
    @MqSubscribe(clientId = "mq", routingKey = "default.exchange:algorithm_status")
    public void receiveMqMsg2(Message message) {
        System.out.println("【算法状态消息】：" + new String(message.getBody(), StandardCharsets.UTF_8));
    }

    /**
     * 监听用户消息（自定义对象参数）
     */
    @MqSubscribe(clientId = "mq", exchange = "default.exchange", queue = "algorithm_status")
    public void receiveMqMsg3(Message message) {
        System.out.println("【算法状态消息1】：" + new String(message.getBody(), StandardCharsets.UTF_8));
    }

    @MqSubscribe(clientId = "mq", queue = "algorithm_control")
    public void receiveMqMsg4(Message message) {
        System.out.println("【算法控制消息1】：" + new String(message.getBody(), StandardCharsets.UTF_8));
    }

    @MqSubscribe(clientId = "mq", queue = "maxinhai")
    public void receiveMqMsg5(Message message) {
        System.out.println("【maxinhai】：" + new String(message.getBody(), StandardCharsets.UTF_8));
    }

}
