package com.zhaododo.core.rest.server.discovery;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.zhaododo.core.rest.server.annotation.ServiceDesc;
import com.zhaododo.core.rest.server.common.LocalIp;
import com.zhaododo.core.rest.server.register.IRegister;

/**
 * 服务注册
 * @author zhaododo
 *
 */
public class SnowServiceDiscovery implements ApplicationContextAware, InitializingBean {

  private final static Logger logger = LoggerFactory.getLogger( SnowServiceDiscovery.class );

  /**
   * spring 注入
   */
  private IRegister register;

  private String context;

  private String port;

  /**
   * 标记了 ServiceDesc.
   */
  private final Map<String /* bean name */, Object /* bean instance */> serviceBeanMap = new HashMap<>();

  /** context --> port */
  private final static Map<String, Integer> context_port_map = Maps.newHashMap();
  static {
    context_port_map.put( "users", 8081 );
    context_port_map.put( "sns", 8082 );
    context_port_map.put( "product", 8083 );
    context_port_map.put( "order", 8084 );
    context_port_map.put( "promotion", 8085 );
    context_port_map.put( "message", 8086 );
    context_port_map.put( "resources", 8087 );
    context_port_map.put( "platform", 8088 );
    context_port_map.put( "bigdata", 8091 );
    context_port_map.put( "brower", 8092 );
    context_port_map.put( "crm", 8093 );
    context_port_map.put( "wechat", 8094 );
    context_port_map.put( "social", 8095 );
    context_port_map.put( "uic", 8096 );

  }

  // 监听地址： 127.0.0.1:8081
  private String listenAddress;

  /**
   * get all beans
   * 
   * @param applicationContext
   * @throws BeansException
   */
  @Override
  public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {

    serviceBeanMap.putAll( applicationContext.getBeansWithAnnotation( Controller.class ) );
    serviceBeanMap.putAll( applicationContext.getBeansWithAnnotation( RestController.class ) );

    logger.info( "End to init service discovery application context, found controller or restcontroller class: {}",
        this.serviceBeanMap );

  }

  @Override
  public void afterPropertiesSet() throws Exception {

    this.listenAddress = LocalIp.getLocalIp() + ":" + getRealPort();

    for ( Object bean : serviceBeanMap.values() ) {

      // 获取service serviceType 如果有YHService的标签，则取标签的值，否则取context
      ServiceDesc annotation = AnnotationUtils.findAnnotation( bean.getClass(), ServiceDesc.class );
      String serviceType = getServiceDescValue( annotation, this.context );

      // 获取controller上的request mapping. @RequestMapping("/value")
      RequestMapping classRequestMappingAnno = AnnotationUtils.findAnnotation( bean.getClass(), RequestMapping.class );
      String class_path = getPath( classRequestMappingAnno );

      // 获取每一个方法上的request mapping的value，可能为空
      Method[] methods = ReflectionUtils.getAllDeclaredMethods( bean.getClass() );
      for ( Method method : methods ) {
        RequestMapping method_requestMappingAnno = method.getAnnotation( RequestMapping.class );
        if ( method_requestMappingAnno != null ) {
          String method_path = getPath( method_requestMappingAnno );

          // method name. 如果有YHService的标签，则取标签的值，否则取方法名字
          ServiceDesc method_anno = method.getAnnotation( ServiceDesc.class );
          String methodName = getServiceDescValue( method_anno, method.getName() );

          if ( method_path.contains( "/query" ) ) {
            logger.info( "methodName {} , method_path {} , class_path {} ", methodName, method_path, class_path );
          }

          this.register.register( this.context, this.listenAddress, serviceType, methodName, method_path, class_path,
              method_anno );
        }

      }

      logger.info( "Register all service for controller:{} success", bean );

    }

  }

  private static String getServiceDescValue( ServiceDesc serviceDesc, String defaultValue ) {
    if ( serviceDesc == null ) {
      return defaultValue;
    }

    String serviceName = serviceDesc.serviceName();
    if ( StringUtils.isNotEmpty( serviceName ) ) {
      return serviceName;
    }

    String value = serviceDesc.value();
    if ( StringUtils.isNotEmpty( value ) ) {
      return value;
    }

    return defaultValue;
  }

  private final static String getPath( RequestMapping requestMapping ) {

    if ( requestMapping == null ) {
      return null;
    }

    String[] path = requestMapping.path();
    if ( ArrayUtils.isNotEmpty( path ) ) {
      return path[0];
    }

    String[] value = requestMapping.value();
    if ( ArrayUtils.isNotEmpty( value ) ) {
      return value[0];
    }

    return null;
  }

  public void setContext( String context ) {
    this.context = context;
  }

  private Integer getRealPort() {
    if ( StringUtils.isNotBlank( port ) ) {
      if ( "default".equals( port ) ) {
        return context_port_map.get( this.context );
      } else {
        return NumberUtils.toInt( port );
      }
    }
    return context_port_map.get( this.context );
  }

  public String getPort() {
    return port;
  }

  public void setPort( String port ) {
    this.port = port;
  }

  public void setRegister( IRegister register ) {
    this.register = register;
  }
}
