package com.logan.waterchat.utils;

import com.logan.waterchat.config.SysConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;


public class ConfigUtils {

    public static HashMap<String, String> parseKeyValueFile(String filePath) throws IOException {
        HashMap<String, String> resultMap = new HashMap<>();
        Properties properties = new Properties();
        
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            properties.load(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
            
            for (String key : properties.stringPropertyNames()) {
                resultMap.put(key, properties.getProperty(key));
            }
        }

        SysConfig.configHashMap = resultMap;
        return resultMap;
    }

}
