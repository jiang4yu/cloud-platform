package com.maxinhai.platform.repository;

import com.maxinhai.platform.po.SysLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLogRepository extends MongoRepository<SysLog, String> {
}
