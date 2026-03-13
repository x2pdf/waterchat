package com.logan.config;

import com.logan.utils.LogUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.*;

public class SysConfigAction {


    public static HashMap<String, String> getAllConfigKeyValue() {
        // 多个模型，配置选项的值
        HashMap<String, String> configHashMap = new HashMap<>();
        try {
            configHashMap = parseKeyValueFile(SysConfig.TEMP_RESOURCES_PATH + SysConfig.CONFIG_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configHashMap;
    }


    @Deprecated
    public static HashMap<String, String> parseKeyValueFile(String filePath) throws IOException {
        HashMap<String, String> resultMap = new HashMap<>();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    resultMap.put(parts[0].trim(), parts[1].trim());
//                    System.out.println(" line: " + line);
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        }

        SysConfig.configHashMap = resultMap;
        return resultMap;
    }


    public static void updateConfigFontSize(int fontSize) {
        String resourcesDirectoryPath = SysConfig.TEMP_RESOURCES_PATH;
        String filePath = resourcesDirectoryPath + SysConfig.CONFIG_PATH;
        SysConfig.FONT_SIZE = fontSize;
        SysConfigAction.updateConfigValue(filePath, "font_size", String.valueOf(fontSize));
    }

    public static void updateConfigAICreativity(double aiCreativity) {
        String resourcesDirectoryPath = SysConfig.TEMP_RESOURCES_PATH;
        String filePath = resourcesDirectoryPath + SysConfig.CONFIG_PATH;
        SysConfig.AI_CREATIVITY = aiCreativity;
        SysConfigAction.updateConfigValue(filePath, "--temp", String.valueOf(aiCreativity));
    }

    public static void updateConfigLanguage(String lang) {
        String resourcesDirectoryPath = SysConfig.TEMP_RESOURCES_PATH;
        String filePath = resourcesDirectoryPath + SysConfig.CONFIG_PATH;
        SysConfig.LANG = lang;
        SysConfigAction.updateConfigValue(filePath, "lang", lang);
    }

    public static void updateConfigValue(String filePath, String key, String newValue) {
        Path path = Paths.get(filePath);
        StringBuilder content = new StringBuilder();
        String prefix = key + "=";
        try {
            // 讀取 - 使用 UTF-8
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(prefix)) {
                        line = prefix + newValue;
                    }
                    content.append(line).append("\n");
                }
            }
            // 寫入 - 使用 UTF-8
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(content.toString());
            }
        } catch (IOException e) {
            LogUtils.error("updateConfigValue error:" + e);
        }
    }

    public static String getLang(String key) {
        String value = SysConfig.LANG_MAP.get(key);
        if (value == null) {
            return key;
        }
        return value;
    }


    public static boolean isMacOS() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            SysConfig.IS_MACOS = false;
            return false;
        }
        SysConfig.IS_MACOS = true;
        return true;
    }

}
