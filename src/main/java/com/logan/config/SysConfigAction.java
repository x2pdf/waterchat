package com.logan.config;

import com.logan.utils.LogUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

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


    /**
     * 向已存在的配置文件末尾追加一行配置
     *
     * @param configFilePath 配置文件完整路径（例如 "config/app.properties"）
     * @param appendStr      要追加的内容，例如 "fontSize=16" 或 "theme.color=#FF5500"
     * @throws IOException 文件读写异常
     */
    public static void appendConfigLine(String configFilePath, String appendStr) throws IOException {
        // 确保追加的内容以换行符结尾（properties 文件通常每行一个配置）
        String lineToAppend = appendStr.trim();
        if (!lineToAppend.isEmpty()) {
            // 如果传入的内容没有包含换行，我们自己加一个
            if (!lineToAppend.endsWith("\n")) {
                lineToAppend += "\n";
            }
            Path path = Paths.get(configFilePath);
            // 方式1：推荐 - 使用 Files.write + APPEND 模式（最简洁，Java 7+）
            Files.write(
                    path,
                    lineToAppend.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE // 如果文件不存在则创建
            );
            LogUtils.info("已追加配置: " + appendStr);
        }
    }


    public static void appendConfigModelNameList(String newModelName) {
        String resourcesDirectoryPath = SysConfig.TEMP_RESOURCES_PATH;
        String filePath = resourcesDirectoryPath + SysConfig.CONFIG_PATH;

        String newModelNameList = "";
        String modelNameList = SysConfig.configHashMap.get("model_name_list");
        if (!modelNameList.isEmpty()) {
            // 如果已经有同名的情形
            if (modelNameList.contains(newModelName)) {
                return;
            }
            newModelNameList = modelNameList + "," + newModelName;
        } else {
            LogUtils.error("model_name_list append error.");
            newModelNameList = newModelName;
        }
        SysConfigAction.updateConfigValue(filePath, "model_name_list", String.valueOf(newModelNameList));
    }


    public static void addConfigModelNameMMProj(String modelName, String nameMmproj) {
        if (nameMmproj != null && !nameMmproj.isEmpty()) {
            String resourcesDirectoryPath = SysConfig.TEMP_RESOURCES_PATH;
            String filePath = resourcesDirectoryPath + SysConfig.CONFIG_PATH;

            // 注意需要添加后缀：  ":mmproj"
            String nameMmprojInConfig = SysConfig.configHashMap.get(modelName + ":mmproj");
            // 已有，就更新替换
            if (nameMmprojInConfig != null) {
                SysConfigAction.updateConfigValue(filePath, modelName + ":mmproj", nameMmproj);
                return;
            }

            try {
                String appendLine = modelName + ":mmproj=" + nameMmproj;
                appendConfigLine(filePath, appendLine);
            } catch (IOException e) {
                LogUtils.error("addConfigModelNameMMProj error: " + e);
            }
        }

    }

    public static void updateConfigFontSize(int fontSize) {
        SysConfig.FONT_SIZE = fontSize;
        SysConfigAction.updateConfigValue(getDefaultConfigPath(), "font_size", String.valueOf(fontSize));
    }

    public static void updateConfigAICreativity(double aiCreativity) {
        SysConfig.AI_CREATIVITY = aiCreativity;
        SysConfigAction.updateConfigValue(getDefaultConfigPath(), "--temp", String.valueOf(aiCreativity));
    }

    public static void updateConfigLanguage(String lang) {
        SysConfig.LANG = lang;
        SysConfigAction.updateConfigValue(getDefaultConfigPath(), "lang", lang);
    }

    public static void updateConfigModelName(String modelName) {
        SysConfig.MODEL_NAME = modelName;
        SysConfigAction.updateConfigValue(getDefaultConfigPath(), "model_name", modelName);
    }

    public static void updateConfigModelPath(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            return;
        }
        // modelName ====> Qwen3.5-0.8B-GGUF/Qwen3.5-0.8B-Q8_0.gguf
        String[] split = modelName.split(File.separator);
        String modelPath = "";
        if (split.length > 0) {
            String modelNamePrefix= split[0];
            modelPath =  "models/" + modelNamePrefix;
        }else{
            return;
        }
        SysConfig.MODEL_PATH = modelPath;
        SysConfigAction.updateConfigValue(getDefaultConfigPath(), "model_path", modelPath);
    }

    public static String getDefaultConfigPath() {
        String resourcesDirectoryPath = SysConfig.TEMP_RESOURCES_PATH;
        return resourcesDirectoryPath + SysConfig.CONFIG_PATH;
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
