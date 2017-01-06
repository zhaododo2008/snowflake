package com.zhaododo.core.zk.client;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;

import com.zhaododo.core.zk.listener.ZKConnectionListener;
import com.zhaododo.core.zk.listener.ZkNodeListener;
import com.zhaododo.core.zk.model.CreateMode;
import com.zhaododo.core.zk.model.Node;

public interface ZKClusterClient {

  void createNode( String path, String value, CreateMode mode );

  String getNodeValue( String path );

  List<Node> getChildNodes( String path );

  void deleteNode( String path );

  void setNodeValue( String path, String value );

  boolean isExsit( String path );

  void registerNodeListener( String path, ZkNodeListener listener );

  void registerConnectionListener( ZKConnectionListener listener );

  boolean isConnected();

  void setConnected( boolean isConnected );

  void shutdown();

  void removeAllWhenConnectionLost();

  List<ZKConnectionListener> getAllZKConnectionListeners();

  CuratorFramework getCuratorFramework();
}
