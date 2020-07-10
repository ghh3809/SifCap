package com.kotoumi.sifcap.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author guohaohao
 */
@Slf4j
public class ConfigHelper {

    private static Properties properties;

    static {
        try {
            properties = new Properties();
            InputStream in = ConfigHelper.class.getClassLoader().getResourceAsStream("config.properties");
            properties.load(in);
        } catch (Exception e) {
            log.error("Could not get config file");
        }
    }

    /**
     * 获取配置
     * @return 配置项
     */
    public static String getProperties(String key){
        return properties.getProperty(key);
    }

}
