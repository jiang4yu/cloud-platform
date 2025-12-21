package com.maxinhai.platform.mapper;

import com.maxinhai.platform.po.JsonRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JsonRecordRepository extends MongoRepository<JsonRecord, String> {
}
