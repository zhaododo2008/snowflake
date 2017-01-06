package com.zhaododo.core.rest.server.utils;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class LocalhostIpFetcher {

    public static String fetchLocalIP() {
        String localIP = "127.0.0.1";
        DatagramSocket sock = null;
        try {
            SocketAddress socket_addr = new InetSocketAddress( InetAddress.getByName("1.2.3.4"), 1);
            sock = new DatagramSocket();
            sock.connect(socket_addr);

            localIP = sock.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            sock.disconnect();
            sock.close();
            sock = null;
        }
        return localIP;
    }
}
