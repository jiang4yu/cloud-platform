package com.maxinhai.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @ClassName：DataReportingApplication
 * @Author: XinHai.Ma
 * @Date: 2026/1/12 21:03
 * @Description: 数据上报模块
 */
@SpringBootApplication
@EnableScheduling
@EnableFeignClients
public class DataReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataReportingApplication.class, args);
    }

}
