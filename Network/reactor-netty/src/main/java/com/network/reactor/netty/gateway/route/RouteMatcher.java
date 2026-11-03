package com.network.reactor.netty.gateway.route;

public class RouteMatcher {

    public static String getServiceName(String path) {

        if (path == null || !path.startsWith("/")) {
            return null;
        }

        String[] parts = path.substring(1).split("/");

        if (parts.length != 2
                || parts[0].isBlank()
                || parts[1].isBlank()) {
            return null;
        }

        return parts[0];
    }

    public static String getMethodName(String path) {

        if (path == null || !path.startsWith("/")) {
            return null;
        }

        String[] parts = path.substring(1).split("/");

        if (parts.length != 2
                || parts[0].isBlank()
                || parts[1].isBlank()) {
            return null;
        }

        return parts[1];
    }

}
