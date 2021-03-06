package com.socialthingy.plusf.spectrum;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.net.URL;

public class Settings {
    private static final Config defaultConfig = ConfigFactory.load("default");
    public static Config config;

    static {
        try {
            final URL latestConfigUrl = new URL("https://raw.githubusercontent.com/alangibson27/plus-f/master/plus-f/src/main/resources/default.conf");
            config = ConfigFactory.parseURL(latestConfigUrl).withFallback(defaultConfig);
        } catch (Exception e) {
            e.printStackTrace();
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
