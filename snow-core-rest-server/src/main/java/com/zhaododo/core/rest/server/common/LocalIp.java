package com.zhaododo.core.rest.server.common;

import com.zhaododo.core.rest.server.utils.LocalhostIpFetcher;

public class LocalIp {

  private static String localIp = LocalhostIpFetcher.fetchLocalIP();

  private LocalIp() {

  }

  /**
   * 获取本机IP地址
   * 
   * @return 本机IP地址
   */
  public static String getLocalIp() {
    return localIp;
  }

}
