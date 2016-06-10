package com.socialthingy.plusf.spectrum;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class Settings {
    private static final Config defaultConfig = ConfigFactory.load("default");
    private static Config config;

    static {
        try {
            final URL latestConfigUrl = new URL("http://download.socialthingy.com/plus-f.conf");
            config = ConfigFactory.parseURL(latestConfigUrl).withFallback(defaultConfig);
        } catch (Exception e) {
            config = defaultConfig;
        }
    }

    public static String DISCOVERY_HOST = config.getString("discovery.host");
    public static int DISCOVERY_PORT = config.getInt("discovery.port");
    public static int COMPUTER_PORT = config.getInt("networking.computer-port");
    public static int GUEST_PORT = config.getInt("networking.guest-port");

    private Settings() {
    }
}
