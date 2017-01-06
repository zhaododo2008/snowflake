package com.zhaododo.core.zk.listener;

import org.apache.curator.framework.state.ConnectionState;

import com.zhaododo.core.zk.client.ZKClusterClient;

public interface ZKConnectionListener {
  /**
   * 连接重建的通知
   * 
   * @param client
   * @param newState
   */
  void stateChanged( ZKClusterClient client, ConnectionState newState );
}
