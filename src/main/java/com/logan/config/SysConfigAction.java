package com.logan.config;

import com.logan.App;
import com.logan.chatui.HelpPage;
import com.logan.utils.LocalFileUtils;
import com.logan.utils.LogUtils;
import javafx.scene.layout.GridPane;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.*;

import static com.logan.config.SysConfig.LANG_CACHE_PATH;

public class SysConfigAction {

    public static void initSysConfigValue() {
        isMacOS();
        createAppLocalPath();
        createAppResourcesPath();
        isConfigExist();
        initModelsNameAndNameList(SysConfig.TEMP_RESOURCES_PATH + SysConfig.CONFIG_PATH);
        initLang();
    }

    public static void refreshConfig() throws IOException {
        // TODO 優化，不必每次更新替換
        updateModelName(SysConfig.MODEL_NAME);
        LogUtils.info("refreshConfig() 配置已更新！");
    }

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
        return resultMap;
    }


    public static void isConfigExist() {
        File file = new File(SysConfig.TEMP_RESOURCES_PATH + SysConfig.CONFIG_PATH);
        if (!file.exists()) {
            // 重新初始化资源文件
            InitSource initSource = new InitSource();
            initSource.init();
            LogUtils.info("initSource 重新初始化资源文件完成。");
        }
    }

    public static void initModelsNameAndNameList(String configPath) {
        // 多个模型，配置选项的值
        HashMap<String, String> configHashMap = null;
        try {
            configHashMap = parseKeyValueFile(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fontSize = configHashMap.get("font_size");
        if (fontSize != null) {
            SysConfig.FONT_SIZE = Integer.parseInt(fontSize);
        }

        String lang = configHashMap.get("lang");
        if (lang != null) {
            SysConfig.LANG = lang;
        }

        String model_name_list = configHashMap.get("model_name_list");
        if (model_name_list != null) {
            String[] strArray = model_name_list.split(",");
            SysConfig.MODEL_NAME_LIST = new ArrayList<>(Arrays.asList(strArray));
        }

        String model_name = configHashMap.get("model_name");
        if (model_name != null) {
            SysConfig.MODEL_NAME = model_name;
        }

        String modelPath = configHashMap.get("model_path");
        if (modelPath != null) {
            SysConfig.MODEL_PATH = modelPath;
        }

        String model_default_system_prompt = configHashMap.get("model_default_system_prompt");
        if (model_default_system_prompt != null) {
            SysConfig.MODEL_DEFAULT_SYSTEM_PROMPT = model_default_system_prompt;
            // 特殊处理
            if (model_default_system_prompt.contains("你是一个博览群书")){
                if (SysConfig.LANG.equals("en")){
                    SysConfig.MODEL_DEFAULT_SYSTEM_PROMPT = "You are an AI who is well-read, " +
                            "knowledgeable in everything from astronomy to geography, " +
                            "and has a deep understanding of all kinds of human experiences. " +
                            "You are not only meticulous and have a high sense of morality, " +
                            "but also enthusiastic and helpful. For every answer you provide, " +
                            "you not only give the most appropriate reply, " +
                            "but also provide users with information or hints that they may need further. " +
                            "You are humanity's best friend!";
                }
            }
        }

    }


    public static void updateModelName(String newValue) {

        String resourcesDirectoryPath = SysConfig.TEMP_RESOURCES_PATH;
        String filePath = resourcesDirectoryPath + SysConfig.CONFIG_PATH;
        String modelPath = resourcesDirectoryPath + "models/" + SysConfig.MODEL_NAME;
        SysConfig.MODEL_NAME = newValue;
        SysConfigAction.updateConfigValue(filePath, "model_path", "" + modelPath);
        SysConfigAction.updateConfigValue(filePath, "model_name", SysConfig.MODEL_NAME);

        App.updateAppName();
        LogUtils.info("现在使用的模型是： " + SysConfig.MODEL_NAME);
        LogUtils.log2LocalLogFile("现在使用的模型是： " + SysConfig.MODEL_NAME);
    }


    public static void updateConfigFontSize(int fontSize) {
        String resourcesDirectoryPath = SysConfig.TEMP_RESOURCES_PATH;
        String filePath = resourcesDirectoryPath + SysConfig.CONFIG_PATH;
        SysConfig.FONT_SIZE = fontSize;
        SysConfigAction.updateConfigValue(filePath, "font_size", String.valueOf(fontSize));
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

    private static void updateLang(String lang) {
        SysConfig.LANG = lang;
    }

    public static void initLang() {
        try {
            // 1. 是否存在缓存文件
            ArrayList<File> filesInFold = LocalFileUtils.getFilesInFold(SysConfig.LANG_CACHE_PATH);
            if (filesInFold == null) {
                HelpPage helpPage = new HelpPage();
                helpPage.changeLangFile(SysConfig.LANG);
            }

            // 2. 读取文件到内存
            ArrayList<File> filesInFold2 = LocalFileUtils.getFilesInFold(SysConfig.APP_DOWNLOAD_PATH + "language" + File.separator);
            // filesInFold2 不应当没有文件， 步骤 1 已经初始化了
            for (File file : filesInFold2) {
                String name = file.getName();
                LogUtils.info("current lang: " + name);
                updateLang(name.split("\\.")[0]);
                Properties pps = new Properties();
                InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                pps.load(in);
                Enumeration<?> en = pps.propertyNames();
                while (en.hasMoreElements()) {
                    String strKey = (String) en.nextElement();
                    String strValue = pps.getProperty(strKey);
                    SysConfig.LANG_MAP.put(strKey, strValue);
                }

                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.info("Lang loading finished");
    }

    /**
     * // APP 要保存的目标路径，默认为用户 Downloads 路径
     *
     * @return
     */
    public static String createAppLocalPath() {
        File home = FileSystemView.getFileSystemView().getHomeDirectory();
        if (isMacOS()) {
            SysConfig.APP_DOWNLOAD_PATH = home.getAbsolutePath() + File.separator + "Downloads" + File.separator + "waterchat" + File.separator;
        } else {
            // C:\\Users\\lance\\Desktop ==> C:\\Users\\lance
            String originalPath = home.getAbsolutePath();
            int lastIndexOfSeparator = originalPath.lastIndexOf("\\");
            String newPath = originalPath.substring(0, lastIndexOfSeparator);
            SysConfig.APP_DOWNLOAD_PATH = newPath + File.separator + "Downloads" + File.separator + "waterchat" + File.separator;
        }

        File file = new File(SysConfig.APP_DOWNLOAD_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        refreshPath();
        return SysConfig.APP_DOWNLOAD_PATH;
    }


    public static String createAppResourcesPath() {
        SysConfig.TEMP_RESOURCES_PATH = SysConfig.APP_DOWNLOAD_PATH + "resources" + File.separator;
        File file = new File(SysConfig.TEMP_RESOURCES_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        return SysConfig.TEMP_RESOURCES_PATH;
    }


    public static boolean isMacOS() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            SysConfig.IS_MACOS = false;
            return false;
        }
        SysConfig.IS_MACOS = true;
        return true;
    }

    public static boolean isNeedInitResources() {
        File file = new File(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH);
        if (file.exists()) {
            return false;
        }
        return true;
    }

    public static void refreshPath() {
        SysConfig.LOG_CACHE_PATH = SysConfig.APP_DOWNLOAD_PATH + "log" + File.separator;
        SysConfig.TEMP_RESOURCES_PATH = SysConfig.APP_DOWNLOAD_PATH + "resources" + File.separator;
    }

}
