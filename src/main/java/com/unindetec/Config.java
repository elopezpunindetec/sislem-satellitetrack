package com.unindetec;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Properties properties = new Properties();

    static {
        try {
            String configFilePath = System.getProperty("config.file");
            InputStream input;
            if (configFilePath != null) {
                input = new FileInputStream(configFilePath);
            } else {
                input = Config.class.getClassLoader().getResourceAsStream("config.properties");
                if (input == null) {
                    throw new RuntimeException("config.properties not found in resources folder");
                }
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading config.properties: " + e.getMessage(), e);
        }
    }

    public static String get(String key) {
        String sysValue = System.getProperty(key);
        return (sysValue != null) ? sysValue : properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null) ? value : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return (value != null) ? Boolean.parseBoolean(value) : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        return (value != null) ? Integer.parseInt(value) : defaultValue;
    }

    public static long getLong(String key, long defaultValue) {
        String value = get(key);
        return (value != null) ? Long.parseLong(value) : defaultValue;
    }
}
