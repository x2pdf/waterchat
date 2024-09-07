package com.logan.config;

import com.logan.App;
import com.logan.utils.LogUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SysConfigAction {

    public static void initSysConfigValue() {
        isMacOS();
        createAppLocalPath();
        createAppResourcesPath();
        isConfigExist();
        initModelsNameAndNameList(SysConfig.TEMP_RESOURCES_PATH + SysConfig.CONFIG_PATH);
    }

    public static void refreshConfig() throws IOException {
        // TODO 優化，不必每次更新替換
        updateModelName(SysConfig.MODEL_NAME);

        LogUtils.info("refreshConfig() 配置已更新！");
    }


    public static HashMap<String, String> parseKeyValueFile(String filePath) throws IOException {
        HashMap<String, String> resultMap = new HashMap<>();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {
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
        String model_name_list = configHashMap.get("model_name_list");
        if (model_name_list != null) {
            String[] strArray = model_name_list.split(",");
            SysConfig.MODEL_NAME_LIST = new ArrayList<>(Arrays.asList(strArray));
        }

        String model_name = configHashMap.get("model_name");
        if (model_name != null) {
            SysConfig.MODEL_NAME = model_name;
        }
    }


    public static void updateModelName(String newValue) throws IOException {

        String resourcesDirectoryPath = SysConfig.TEMP_RESOURCES_PATH;
        String filePath = resourcesDirectoryPath + SysConfig.CONFIG_PATH;
        String modelPath = resourcesDirectoryPath + "models/" + SysConfig.MODEL_NAME;
        SysConfig.MODEL_NAME = newValue;
        SysConfigAction.updateConfigValue(filePath, "model_path", "" + modelPath);
        SysConfigAction.updateConfigValue(filePath, "model_name", SysConfig.MODEL_NAME);

        App.updateAppName();
        LogUtils.info("现在使用的模型是： " + SysConfig.MODEL_NAME);
    }

    public static void updateConfigValue(String filePath, String key, String newValue) throws IOException {
        // 读取文件内容
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(key + "=")) {
                line = key + "=" + newValue;
            }
            content.append(line).append("\n");
        }
        reader.close();
        // 写入修改后的内容
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(content.toString());
        writer.close();
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
        SysConfig.TEMP_RESOURCES_PATH = SysConfig.APP_DOWNLOAD_PATH + File.separator + "resources" + File.separator;
        File file = new File(SysConfig.TEMP_RESOURCES_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        return SysConfig.TEMP_RESOURCES_PATH;
    }

    public static String getModelExecSourcePath() {
        String modelExecSourcePath = SysConfig.MODEL_EXEC_SOURCE_PATH_MAC;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            modelExecSourcePath = SysConfig.MODEL_EXEC_SOURCE_PATH;
        }
        return modelExecSourcePath;
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
