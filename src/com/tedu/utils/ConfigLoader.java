package com.tedu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 通用配置加载工具类，可加载任意properties文件，并支持缓存。
 */
public class ConfigLoader {
    private static final Properties plantProps = new Properties();
    private static final Properties zombieProps = new Properties();

    static {
        try {
            // 加载植物配置
            InputStream plantIn = ConfigLoader.class.getClassLoader().getResourceAsStream("com/tedu/text/plants.properties");
            if (plantIn != null) {
                plantProps.load(plantIn);
                plantIn.close();
            }
            // 加载僵尸配置
            InputStream zombieIn = ConfigLoader.class.getClassLoader().getResourceAsStream("com/tedu/text/zombies.properties");
            if (zombieIn != null) {
                zombieProps.load(zombieIn);
                zombieIn.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPlantProperty(String key) {
        return plantProps.getProperty(key);
    }

    public static String getZombieProperty(String key) {
        return zombieProps.getProperty(key);
    }

    public static int getPlantInt(String key, int defaultValue) {
        String value = getPlantProperty(key);
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int getZombieInt(String key, int defaultValue) {
        String value = getZombieProperty(key);
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // 可根据需要扩展更多类型的读取方法
} 