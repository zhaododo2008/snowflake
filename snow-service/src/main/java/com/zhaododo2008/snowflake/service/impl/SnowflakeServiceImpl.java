package com.zhaododo2008.snowflake.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zhaododo2008.snowflake.service.ISnowflakeService;
import com.zhaododo2008.snowflake.service.IdWorker;

@Service
public class SnowflakeServiceImpl implements ISnowflakeService {

  public static final Logger logger = LoggerFactory.getLogger(SnowflakeServiceImpl.class);

  private final IdWorker idWorker = new IdWorker(0,1);
  
  @Override
  public long getSequence() {
    long sequence = idWorker.nextId();
    logger.info( "getSequence {}",sequence );
    return sequence;
  }

}
