package com.zhaododo.core.zk.factory;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 解析zk-cluster.xml配置文件
 * 先加载/conf/zk-server.properties，如果不存在就默认加载META-INF/zk-server.properties下的
 * 
 * @author zhaododo
 *
 */
public class ZKConfigParser {

  private final static Logger log = LoggerFactory.getLogger( ZKConfigParser.class );

  private static Properties p = new Properties();

  private static ZkConfig zkConfig = new ZkConfig();

  public synchronized static ZkConfig parser() throws Exception {

    InputStream inputStream = null;
    try {
      inputStream = ZKConfigParser.class.getResourceAsStream( "/zk-server.properties" );
      p.load( inputStream );
      // 名字
      zkConfig.setName( p.getProperty( "zkName" ).trim() );
      zkConfig.setConnectionTimeoutMs( Integer.valueOf( p.getProperty( "zkConnectionTimeoutMs" ).trim() ) );
      zkConfig.setRetrySleepTimeMs( Integer.valueOf( p.getProperty( "zkRetrySleepTimeMs" ).trim() ) );
      zkConfig.setRetryTimes( Integer.valueOf( p.getProperty( "zkRetryTimes" ).trim() ) );
      zkConfig.setSessionTimeoutMs( Integer.valueOf( p.getProperty( "zkSessionTimeoutMs" ).trim() ) );
      zkConfig.setUrl( p.getProperty( "zkServer" ).trim() );
    } catch ( Exception e ) {
      log.error( "parser file zk-server.properties failed!!!", e );
      throw e;
    } finally {
      IOUtils.closeQuietly( inputStream );
    }
    return zkConfig;
  }

  public static String getZkName() {
    return zkConfig.getName();
  }

  public static String getZkServer() {
    return zkConfig.getUrl();
  }
}
