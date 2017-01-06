package com.zhaododo.core.zk.factory;

import java.text.MessageFormat;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.zhaododo.core.zk.client.ZKClusterClient;
import com.zhaododo.core.zk.client.impl.DefaultZKClusterClient;
import com.zhaododo.core.zk.exception.ZKConnectException;
import com.zhaododo.core.zk.listener.DefaultConnectionListener;

public class ZookeeperClientMakerFactory {

  private final Logger log = LoggerFactory.getLogger( getClass() );

  private ConcurrentMap<String, ZKClusterClient> clientFactory = Maps.newConcurrentMap();

  private ConcurrentMap<CuratorFramework, ZKClusterClient> cur_clientFactory = Maps.newConcurrentMap();

  private AtomicBoolean inited = new AtomicBoolean( false );

  private final static ZookeeperClientMakerFactory _factory = new ZookeeperClientMakerFactory();

  private final static Integer RETRY_SLEEPTIMEMS = 1000;

  private final static Integer RETRY_TIMES = 3;

  private ZookeeperClientMakerFactory() {

  }

  public synchronized static ZookeeperClientMakerFactory getInstance() {
    return _factory;
  }

  public ZKClusterClient getZKClusterClient( String name ) {
    ZKClusterClient client = clientFactory.get( name );
    if ( null == client ) {
      throw new IllegalArgumentException( "name may be wrong name is：" + name );
    }
    if ( !client.isConnected() ) {
      throw new ZKConnectException( "with zk server connection loss,please check!!!!" );
    }
    return client;
  }

  public void init( ZkConfig congfig ) {
    if ( inited.compareAndSet( false, true ) ) {
      // 校验配置是否合法
      validate( congfig );
      RetryPolicy retryPolicy =
          new ExponentialBackoffRetry( null == congfig.getRetrySleepTimeMs() ? RETRY_SLEEPTIMEMS : congfig
              .getRetrySleepTimeMs(), null == congfig.getRetryTimes() ? RETRY_TIMES : congfig.getRetryTimes() );
      ZKClusterClient client =
          createZkClient( congfig.getUrl(), retryPolicy, congfig.getConnectionTimeoutMs(), congfig
              .getSessionTimeoutMs() );
      clientFactory.put( congfig.getName(), client );
    }
  }

  private ZKClusterClient createZkClient( String connectionString, RetryPolicy retryPolicy, int connectionTimeoutMs,
      int sessionTimeoutMs ) {
    CuratorFramework curatorFramework =
        CuratorFrameworkFactory.builder().connectString( connectionString ).retryPolicy( retryPolicy )
            .connectionTimeoutMs( connectionTimeoutMs ).sessionTimeoutMs( sessionTimeoutMs ).build();
    final CountDownLatch downLactch = new CountDownLatch( 1 );
    curatorFramework.getConnectionStateListenable().addListener( new ConnectionStateListener() {

      @Override
      public void stateChanged( CuratorFramework client, ConnectionState newState ) {
        if ( newState == ConnectionState.CONNECTED || newState == ConnectionState.RECONNECTED ) {
          downLactch.countDown();
          log.info( "with zk server connection is ok!!!!" );
        }
      }
    } );
    curatorFramework.getUnhandledErrorListenable().addListener( new UnhandledErrorListener() {
      @Override
      public void unhandledError( String message, Throwable e ) {
        log.info( "CuratorFramework unhandledError: {}", message );
      }
    } );
    curatorFramework.start();
    try {
      downLactch.await( connectionTimeoutMs, TimeUnit.MILLISECONDS );
    } catch ( InterruptedException e ) {
      log.warn( "connection the url:{} failed at connectionTimeoutMs:{}", connectionString, connectionTimeoutMs );
      String message =
          MessageFormat.format( "connection the url:{0} failed at connectionTimeoutMs:{1}", connectionString,
              connectionTimeoutMs );
      throw new ZKConnectException( message, e );
    }
    ZKClusterClient client = new DefaultZKClusterClient( curatorFramework );
    client.setConnected( true );
    cur_clientFactory.put( client.getCuratorFramework(), client );
    // 注册默认监听
    curatorFramework.getConnectionStateListenable().addListener( new DefaultConnectionListener() );
    return client;
  }

  public ZKClusterClient createZkClient( String connectionString, int connectionTimeoutMs, int sessionTimeoutMs ) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry( RETRY_SLEEPTIMEMS, RETRY_TIMES );
    return this.createZkClient( connectionString, retryPolicy, connectionTimeoutMs, sessionTimeoutMs );
  }

  private void validate( ZkConfig congfig ) {
    if ( null == congfig ) {
      throw new IllegalArgumentException( "congfig can't be null" );
    }
    if ( StringUtils.isBlank( congfig.getUrl() ) || StringUtils.isBlank( congfig.getName() ) ) {
      throw new IllegalArgumentException( "name or url can't be null" );
    }
    if ( null == congfig.getConnectionTimeoutMs() || null == congfig.getSessionTimeoutMs() ) {
      throw new IllegalArgumentException( "connectionTimeoutMs or sessionTimeoutMs can't be null" );
    }
  }

  public ZKClusterClient getZKClusterClient( CuratorFramework client ) {
    return cur_clientFactory.get( client );
  }

}
