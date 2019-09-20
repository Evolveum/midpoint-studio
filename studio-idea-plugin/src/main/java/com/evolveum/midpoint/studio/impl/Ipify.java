package com.evolveum.midpoint.studio.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Ipify {

    private static final String IPIFY_HOST = "api.ipify.org";

    private Ipify() {
    }

    public static String getPublicIp() throws IOException {
        return getPublicIp(false);
    }

    public static String getPublicIp(boolean useHttps) throws IOException {
        String url = useHttps ? "https://" + IPIFY_HOST : "http://" + IPIFY_HOST;

        URL ipify = new URL(url);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ipify.openStream()))) {
            return reader.readLine();
        }
    }
}
