package com.logan.chat;

import com.logan.config.SysConfig;
import com.logan.utils.LogUtils;

public class LLaMAConf {

    // macOS Apple Silicon (arm64)
    private static final String LLaMA_MAC_ARM64 = SysConfig.MODEL_EXEC_PATH + "/llama-mac-arm64";
    // macOS Intel (x64)
    private static final String LLaMA_MAC_x64 = SysConfig.MODEL_EXEC_PATH + "/llama-mac-x64";
    // Windows x64 (CPU)
    private static final String LLaMA_WIN_CPU_x64 = SysConfig.MODEL_EXEC_PATH + "/llama-win-cpu-x64";
    // Windows arm64 (CPU)
    private static final String LLaMA_WIN_CPU_ARM64 = SysConfig.MODEL_EXEC_PATH + "/llama-win-cpu-arm64";
    // Windows x64 (Vulkan)
    private static final String LLaMA_WIN_VULKAN_x64 = SysConfig.MODEL_EXEC_PATH + "/llama-win-vulkan-x64";

    public static String LLAMA_SERVER_HOST = "127.0.0.1";
    public static String LLAMA_SERVER_PORT = "9088";
    public static String LLAMA_SERVER_BASE_URL = "http://" + LLAMA_SERVER_HOST +":" + LLAMA_SERVER_PORT + "/v1/chat/completions";
    public static String LLAMA_SERVER_BASE_MODEL = "Qwen3-0.6B-GGUF";

    public static String getLLaMAExecPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        // macOS
        if (os.contains("mac")) {
            if (isAppleSilicon()) {
                return LLaMA_MAC_ARM64;
            } else {
                return LLaMA_MAC_x64;
            }
        }

        // Windows
        if (os.contains("win")) {
            if (arch.contains("aarch64") || arch.contains("arm64")) {
                return LLaMA_WIN_CPU_ARM64;
            }
            if (arch.contains("64")) {
                // Vulkan
                if (SysConfig.IS_USE_VULKAN){
                    return LLaMA_WIN_VULKAN_x64;
                }
                return LLaMA_WIN_CPU_x64;
            }
        }

        return LLaMA_WIN_CPU_x64;
    }

    public static String getLLaMAExecAbsPath() {
        return SysConfig.TEMP_RESOURCES_PATH + getLLaMAExecPath();
    }


    private static boolean isAppleSilicon() {
        try {
            Process process = new ProcessBuilder(
                    "sysctl", "-in", "sysctl.proc_translated"
            ).start();
            try (java.io.BufferedReader reader =
                         new java.io.BufferedReader(
                                 new java.io.InputStreamReader(process.getInputStream()))) {
                String result = reader.readLine();
                //1 → 当前进程被 Rosetta 转译（说明真实是 ARM）
                //0 → 原生运行
                return "1".equals(result); // 被 Rosetta 转译 = Apple Silicon
            }
        } catch (Exception e) {
            return false;
        }
    }

}
