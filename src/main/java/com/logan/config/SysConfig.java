package com.logan.config;


import java.util.ArrayList;

public class SysConfig {
    public static String APP_NAME = "WaterChat";
    public static String MODEL_NAME = "Qwen2-0.5B-Instruct";
    public static String MODEL_PATH = "models/Qwen2-0.5B-Instruct";
    public static String MODEL_EXEC_SOURCE_PATH = "modelsexec/call_chat_v1.exe";
    public static String MODEL_EXEC_SOURCE_PATH_MAC = "modelsexec/call_chat_v1";
    public static ArrayList<String> MODEL_NAME_LIST = new ArrayList<>();

    public static boolean IS_MACOS = true;
    public static String MESSAGES = "";
    public static String CONFIG_PATH = "config/config.txt";
    public static String RESOURCE_MOVE_CONFIG_PATH = "config/resourcefilepath.txt";
    public static String RESPONSE_PATH = "";
    public static String RESPONSE_FILENAME = "response.txt";
    public static String MAX_NEW_TOKENS = "512";
    public static String MESSAGE_FILENAME = "message.json";
    public static String MODEL_EXEC_PATH = "modelsexec";


    public static boolean IS_LOG_SESSION = true;
    // APP 要保存的目标路径，默认为用户 Downloads 路径
    public static String APP_DOWNLOAD_PATH = "";


    public static double MARGIN_DEFAULT = 2.0;
    // 初始化TextArea的高度
    public static int TEXT_AREA_INPUT_BOX_ROW = 4;
    // 高度
    public static int STAGE_HEIGHT = 600;
    // 宽度
    public static int STAGE_WIDTH = 800;

    // APP log 的缓存路径
    public static String LOG_CACHE_PATH = "";

    public static String TEMP_RESOURCES_PATH = "";

    // 日志的文件名称
    public static String SESSION_LOG_FILE_NAME = "water_chat_session_log.txt";

}
