package com.logan.waterchat.config;


import java.util.ArrayList;
import java.util.HashMap;

public class SysConfig {
    public static String APP_NAME = "WaterChat";
    public static String MODEL_NAME = "Qwen3.5-0.8B-GGUF/Qwen3.5-0.8B-Q8_0.gguf";  // 需要指定到 xxxx.gguf 文件为止
    public static String MODEL_PATH = "Qwen3.5-0.8B-GGUF/Qwen3.5-0.8B-Q8_0.gguf";  // 需要指定到 xxxx.gguf 文件为止
    public static ArrayList<String> MODEL_NAME_LIST = new ArrayList<>();
    public static String MODEL_DEFAULT_SYSTEM_PROMPT = "你是一个博览群书、上知天文下知地理、深刻理解人类世界各种经验的AI，" +
            "你不仅心思缜密，有崇高的道德感，还洋溢热情乐于助人，" +
            "对于每一次提供的回答不仅提供最恰当最严谨的直切要点的回复，同时还提供给用户可能进一步需要的信息或提示，你是人类最好的朋友！";

    public static boolean IS_MACOS = true;
    public static String CONFIG_PATH = "config/config.txt";
    public static String RESOURCE_MOVE_CONFIG_PATH = "config/resourcefilepath.txt";
    public static String MODEL_EXEC_PATH = "modelsexec";


    public static boolean IS_LOG_SESSION = true;
    public static boolean IS_USE_VULKAN = false;
    // APP 要保存的目标路径，默认为用户 Downloads 路径
    public static String APP_DOWNLOAD_PATH = "";


    // APP log 的缓存路径
    public static String LOG_CACHE_PATH = "";

    public static String TEMP_RESOURCES_PATH = "";

    // 应用语言,默认cn, 可选的值为：cn, en
    public static String LANG = "cn";
    // 语言map，映射语言使用
    public static HashMap<String, String> LANG_MAP = new HashMap<>();


    // AI对话日志的文件名称
    public static String SESSION_LOG_FILE_NAME = "chat_log_water_chat_session.txt";

    // 应用日志的文件名称
    public static String APP_LOG_FILE_NAME = "app_server_log.txt";

    public static HashMap<String, String> configHashMap = new HashMap<>();

}
