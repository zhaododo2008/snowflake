package com.zhaododo.core.zk.listener;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zhaododo.core.zk.model.ZkNodeData;
import com.zhaododo.core.zk.util.ZkPathUtils;

public class DefaultNodeCacheListener implements NodeCacheListener, PathChildrenCacheListener {

  private final Logger log = LoggerFactory.getLogger( getClass() );

  private Map<String, Set<ZkNodeListener>> zkNodeListenerMap = Maps.newConcurrentMap();

  private CuratorFramework curator;

  private NodeCache nodeCache;

  private Charset charset = Charset.forName( "UTF-8" );

  @SuppressWarnings( "resource" )
  public DefaultNodeCacheListener( String path, CuratorFramework curator ) {
    this.curator = curator;
    nodeCache = new NodeCache( curator, path );
    PathChildrenCache pathNode = new PathChildrenCache( curator, path, true );
    try {
      nodeCache.start();
      pathNode.start( StartMode.BUILD_INITIAL_CACHE );
    } catch ( Exception e ) {
      log.error( "node start failed", e );
    }
    nodeCache.getListenable().addListener( this );
    pathNode.getListenable().addListener( this );
  }

  @Override
  public void childEvent( CuratorFramework client, PathChildrenCacheEvent event ) throws Exception {
    if ( null == event.getData() || null == event.getData().getData() ) {
      log.warn( "childEvent fired failed,the data is null!!!" );
    }
    byte[] data = event.getData().getData();
    String path = event.getData().getPath();
    ZkNodeData nodeData = new ZkNodeData( path, new String( data, charset ), null );
    Set<ZkNodeListener> zkNodeListeners = zkNodeListenerMap.get( path );
    // 获取对父节点的监听
    Set<ZkNodeListener> parentZKNodeListeners = zkNodeListenerMap.get( ZkPathUtils.getParentPath( path ) );
    Set<ZkNodeListener> all_listeners = new HashSet<ZkNodeListener>();
    if ( CollectionUtils.isNotEmpty( zkNodeListeners ) ) {
      all_listeners.addAll( zkNodeListeners );
    }
    if ( CollectionUtils.isNotEmpty( parentZKNodeListeners ) ) {
      all_listeners.addAll( parentZKNodeListeners );
    }
    if ( CollectionUtils.isEmpty( all_listeners ) ) {
      log.info( "no listeners for this path:{}", path );
      return;
    }
    invokeZkNodeListener( event, nodeData, all_listeners );
  }

  private void invokeZkNodeListener( PathChildrenCacheEvent event, ZkNodeData nodeData,
      Set<ZkNodeListener> all_listeners ) {
    if ( event.getType().equals( Type.CHILD_ADDED ) ) {
      for ( ZkNodeListener zkNodeListener : all_listeners ) {
        if ( zkNodeListener.accept( nodeData ) ) {
          zkNodeListener.childAdded( nodeData );
        }
      }
    } else if ( event.getType().equals( Type.CHILD_UPDATED ) ) {
      for ( ZkNodeListener zkNodeListener : all_listeners ) {
        if ( zkNodeListener.accept( nodeData ) ) {
          zkNodeListener.childUpdated( nodeData );
        }
      }
    } else if ( event.getType().equals( Type.CHILD_REMOVED ) ) {
      for ( ZkNodeListener zkNodeListener : all_listeners ) {
        if ( zkNodeListener.accept( nodeData ) ) {
          zkNodeListener.childDeleted( nodeData );
        }
      }
    } else {
      log.error( "no type for this:{}", event.getType() );
    }
  }

  @Override
  public void nodeChanged() throws Exception {
    byte[] data = nodeCache.getCurrentData().getData();
    String path = nodeCache.getCurrentData().getPath();
    Set<ZkNodeListener> zkNodeListeners = zkNodeListenerMap.get( path );
    ZkNodeData nodeData = new ZkNodeData( path, new String( data, charset ), null );
    if ( CollectionUtils.isNotEmpty( zkNodeListeners ) ) {
      for ( ZkNodeListener zkNodeListener : zkNodeListeners ) {
        if ( zkNodeListener.accept( nodeData ) ) {
          zkNodeListener.nodeUpdated( nodeData );
        }
      }
    }
  }

  public void registerNodeListener( String path, ZkNodeListener listener ) {
    Set<ZkNodeListener> zkNodeListeners = zkNodeListenerMap.get( path );
    if ( CollectionUtils.isEmpty( zkNodeListeners ) ) {
      zkNodeListeners = Sets.newHashSet( listener );
      zkNodeListenerMap.put( path, zkNodeListeners );
    } else {
      zkNodeListeners.add( listener );
    }
  }

  public CuratorFramework getCurator() {
    return curator;
  }
}
