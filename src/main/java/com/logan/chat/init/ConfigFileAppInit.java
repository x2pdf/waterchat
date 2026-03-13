package com.logan.chat.init;

import com.logan.App;
import com.logan.chat.llamaccp.LLaMAConf;
import com.logan.chatui.HelpPage;
import com.logan.config.InitSourceTemplate;
import com.logan.config.SysConfig;
import com.logan.utils.AppSystemOS;
import com.logan.utils.ConfigUtils;
import com.logan.utils.LocalFileUtils;
import com.logan.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigFileAppInit implements AppInitInterface {

    @Override
    public void init() {
        try {
            initConfigValue();
            if (!isConfigExist()) {
                // 说明资源文件被删除之类的，需要重新走流程将资源文件复制到电脑本地
                InitSourceTemplate initSourceTemplate = new InitSourceTemplate();
                initSourceTemplate.init();
            }
            initSystemConfigValue(SysConfig.TEMP_RESOURCES_PATH + SysConfig.CONFIG_PATH);
            initLang();
        } catch (Exception e) {
            LogUtils.error("ConfigFileAppInit error: " + e);
        }
    }


    public void initConfigValue() {
        if (AppSystemOS.isMacOS()) {
            SysConfig.IS_MACOS = true;
        } else {
            SysConfig.IS_MACOS = false;
        }

    }


    public static boolean isConfigExist() {
        File file = new File(SysConfig.TEMP_RESOURCES_PATH + SysConfig.CONFIG_PATH);
        return file.exists();
    }


    public static void initSystemConfigValue(String configPath) {
        // 多个模型，配置选项的值
        HashMap<String, String> configHashMap = null;
        try {
            configHashMap = ConfigUtils.parseKeyValueFile(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fontSize = configHashMap.get("font_size");
        if (fontSize != null) {
            SysConfig.FONT_SIZE = Integer.parseInt(fontSize);
        }

        String aiCreativity = configHashMap.get("--temp");
        if (aiCreativity != null) {
            SysConfig.AI_CREATIVITY = Double.parseDouble(aiCreativity);
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

        String modelMmprojFile = configHashMap.get(SysConfig.MODEL_NAME + ":mmproj");
        if (modelMmprojFile != null && !modelMmprojFile.isEmpty()) {
            LLaMAConf.IS_MODEL_CONTAIN_MMPORJ_FILE = true;
            LLaMAConf.MODEL_MMPORJ_FILENAME = modelMmprojFile;
        }

        String model_default_system_prompt = configHashMap.get("model_default_system_prompt");
        if (model_default_system_prompt != null) {
            SysConfig.MODEL_DEFAULT_SYSTEM_PROMPT = model_default_system_prompt;
            // 特殊处理
            if (model_default_system_prompt.contains("你是一个博览群书")) {
                if (SysConfig.LANG.equals("en")) {
                    SysConfig.MODEL_DEFAULT_SYSTEM_PROMPT = "You are an AI who is well-read, " +
                            "knowledgeable in everything from astronomy to geography, " +
                            "and has a deep understanding of all kinds of human experiences. " +
                            "You are not only meticulous and have a high sense of morality, " +
                            "but also enthusiastic and helpful. For each answer provided, " +
                            "not only the most appropriate and rigorous response to the point is provided," +
                            "but also provide users with information or hints that they may need further. " +
                            "You are humanity's best friend!";
                }
            }
        }
    }


    public static void initLang() {
        try {
            // 1. 是否存在缓存文件
            ArrayList<File> filesInFold = LocalFileUtils.getFilesInFold(SysConfig.APP_DOWNLOAD_PATH + "language" + File.separator);
            if (filesInFold == null) {
                ResourcesFileAppInit resourcesFileAppInit = new ResourcesFileAppInit();
                resourcesFileAppInit.changeLangFile(SysConfig.LANG);
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
            LogUtils.error("initLang: " + e);
        }
        LogUtils.info("Lang loading finished");
    }


    private static void updateLang(String lang) {
        SysConfig.LANG = lang;
    }

}
