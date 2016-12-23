package com.zhaododo2008.snowflake.restapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhaododo2008.snowflake.service.ISnowflakeService;

@RequestMapping("/home")
@RestController
public class SnowflakeController {
  
  public static final Logger logger = LoggerFactory.getLogger(SnowflakeController.class);

  @Autowired
  ISnowflakeService snowflakeService;
  
  @RequestMapping("/getSequence")
  public long getSequence() {
    return snowflakeService.getSequence();
  }

}
