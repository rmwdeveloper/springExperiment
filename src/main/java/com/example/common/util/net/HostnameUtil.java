package com.example.common.util.net;

import java.net.InetAddress;

public class HostnameUtil {

    private static InetAddress thisInetAddress;
    private static String thisHostName;
    private static String thisCanonicalHostName;

    static {
        try {
            HostnameUtil.thisInetAddress = InetAddress.getLocalHost();
            HostnameUtil.thisHostName = HostnameUtil.thisInetAddress.getHostAddress();
            HostnameUtil.thisCanonicalHostName = HostnameUtil.thisInetAddress.getCanonicalHostName();
        } catch (Exception e) {
            throw new RuntimeException("Can not determine host information.", e);
        }
    }

    public static InetAddress getMyInetAddress() {
        return HostnameUtil.thisInetAddress;
    }

    public static String getMyHostName() {
        return HostnameUtil.thisHostName;
    }

    public static String getMyCanonicalHostName() {
        return HostnameUtil.thisCanonicalHostName;
    }

}
