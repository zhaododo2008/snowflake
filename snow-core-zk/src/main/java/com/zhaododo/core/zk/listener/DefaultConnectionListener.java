package com.zhaododo.core.zk.listener;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhaododo.core.zk.client.ZKClusterClient;
import com.zhaododo.core.zk.factory.ZookeeperClientMakerFactory;

public class DefaultConnectionListener implements ConnectionStateListener {

  private final Logger log = LoggerFactory.getLogger( getClass() );

  @Override
  public void stateChanged( CuratorFramework client, ConnectionState newState ) {

    boolean isConnected = false;
    if ( newState == ConnectionState.CONNECTED || newState == ConnectionState.RECONNECTED ) {
      isConnected = true;

    } else if ( newState == ConnectionState.SUSPENDED || newState == ConnectionState.LOST ) {
      isConnected = false;
      log.error( "with zk connection loss stat:{}", newState );
    }
    onRefersh( client, newState, isConnected );
  }

  private void onRefersh( CuratorFramework client, ConnectionState newState, boolean isConnected ) {
    ZKClusterClient zkClient = ZookeeperClientMakerFactory.getInstance().getZKClusterClient( client );
    if ( zkClient.isConnected() && isConnected ) {
      // donothing
    } else if ( !zkClient.isConnected() && isConnected ) {
      // 设置连接为已经连接状态
      zkClient.setConnected( true );
      List<ZKConnectionListener> zkConnectionListeners = zkClient.getAllZKConnectionListeners();
      for ( ZKConnectionListener zkConnectionListener : zkConnectionListeners ) {
        zkConnectionListener.stateChanged( zkClient, newState );
      }
    } else {
      // 连接断掉
      zkClient.setConnected( false );
      List<ZKConnectionListener> zkConnectionListeners = zkClient.getAllZKConnectionListeners();
      for ( ZKConnectionListener zkConnectionListener : zkConnectionListeners ) {
        zkConnectionListener.stateChanged( zkClient, newState );
      }
      zkClient.removeAllWhenConnectionLost();
    }
  }
}
