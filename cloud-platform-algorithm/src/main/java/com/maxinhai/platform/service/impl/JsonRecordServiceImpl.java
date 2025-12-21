package com.maxinhai.platform.service.impl;

import com.maxinhai.platform.mapper.JsonRecordRepository;
import com.maxinhai.platform.po.JsonRecord;
import com.maxinhai.platform.service.JsonRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonRecordServiceImpl implements JsonRecordService {

    private final JsonRecordRepository jsonRecordRepository;

    @PostConstruct
    public void initData() {
        for (int i = 1; i <= 100; i++) {
            JsonRecord jsonRecord = new JsonRecord();
            jsonRecord.setKey("key_" + i);
            jsonRecord.setField("field_" + i);
            jsonRecord.setValue(String.valueOf(i));
            jsonRecord.setValueType("Integer");
            jsonRecordRepository.save(jsonRecord);
        }
    }

}
