package com.zhaododo.core.zk.listener;

import com.zhaododo.core.zk.model.ZkNodeData;

public interface ZkNodeListener {

  void childUpdated( ZkNodeData nodeData );

  void childAdded( ZkNodeData nodeData );

  void childDeleted( ZkNodeData nodeData );

  void nodeUpdated( ZkNodeData nodeData );

  boolean accept( ZkNodeData nodeData );
}
