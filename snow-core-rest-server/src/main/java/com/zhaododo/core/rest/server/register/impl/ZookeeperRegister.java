package com.zhaododo.core.rest.server.register.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhaododo.core.rest.server.annotation.ServiceDesc;
import com.zhaododo.core.rest.server.common.InstanceDetail;
import com.zhaododo.core.rest.server.common.YHJsonInstanceSerializer;
import com.zhaododo.core.rest.server.register.IRegister;

/**
 * zookeeper注册
 * 
 * @author zhaododo
 *
 */
public class ZookeeperRegister implements IRegister {

  private final static Logger logger = LoggerFactory.getLogger( ZookeeperRegister.class );

  private final ServiceDiscovery<InstanceDetail> serviceDiscovery;

  /**
   * 服务注册
   * 
   * @param client
   *          curator客户端
   * @param basePath
   *          zk的path前缀
   * @throws Exception
   */
  public ZookeeperRegister( CuratorFramework client, String basePath ) throws Exception {
    // JSON序列化
    YHJsonInstanceSerializer<InstanceDetail> serializer =
        new YHJsonInstanceSerializer<InstanceDetail>( InstanceDetail.class );
    serviceDiscovery =
        ServiceDiscoveryBuilder.builder( InstanceDetail.class ).client( client ).serializer( serializer ).basePath(
            basePath ).build();
    serviceDiscovery.start();

    logger.info( "zookeeper ServiceRegister start success" );
  }

  @Override
  public void register( String context, String listenAddress, String serviceType, String methodName, String method_path,
      String controller_path, ServiceDesc serviceDesc ) {

    boolean degrade = serviceDesc == null ? false : serviceDesc.degrade();
    InstanceDetail.InstanceDetailBuilder builder = new InstanceDetail.InstanceDetailBuilder();
    builder.linstenAddress( listenAddress ).context( context ).serviceType( serviceType ).methodName( methodName )
        .controllerRequestMapping( controller_path ).methodReuqestMapping( method_path ).degrade( degrade );

    InstanceDetail instanceDetail = builder.build();
    try {
      ServiceInstance<InstanceDetail> serviceInstance =
          ServiceInstance.<InstanceDetail> builder().name( instanceDetail.getServiceName() ).address( listenAddress
              .split( ":" )[0] ).port( Integer.parseInt( listenAddress.split( ":" )[1] ) ).payload( instanceDetail )
              .build();

      this.serviceDiscovery.registerService( serviceInstance );
    } catch ( Exception e ) {
      logger.error( "register service : {} error.", e.getMessage() );
    }
  }
}
