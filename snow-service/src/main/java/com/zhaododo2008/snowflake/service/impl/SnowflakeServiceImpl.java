package com.zhaododo2008.snowflake.service.impl;

import org.springframework.stereotype.Service;

import com.zhaododo2008.snowflake.service.ISnowflakeService;
import com.zhaododo2008.snowflake.service.IdWorker;

@Service
public class SnowflakeServiceImpl implements ISnowflakeService {

  private final IdWorker idWorker = new IdWorker(0,1);
  
  @Override
  public long getSequence() {
    return idWorker.nextId();
  }

}
