package com.logan.waterchat.chat.llamaccp;


import com.logan.waterchat.config.SysConfig;

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

    public static boolean IS_LLAMA_SERVER_STARTED = false;
    public static String LLAMA_SERVER_HOST = "127.0.0.1";
    public static String LLAMA_SERVER_PORT = "9088";
    public static String LLAMA_SERVER_BASE_URL = "http://" + LLAMA_SERVER_HOST +":" + LLAMA_SERVER_PORT + "/v1/chat/completions";
    public static String LLAMA_SERVER_BASE_MODEL = "Qwen3.5-0.8B-GGUF/Qwen3.5-0.8B-Q8_0.gguf";

    public static boolean IS_MODEL_CONTAIN_MMPORJ_FILE = false;
    public static String MODEL_MMPORJ_FILENAME = "";

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
        String osName = System.getProperty("os.name").toLowerCase();
        // 1. 首先判断是否为 macOS 系统
        if (!osName.contains("mac")) {
            return false;
        }

        // 2. 检查 JVM 自身的架构（如果是原生 aarch64 JDK，直接判定为 Apple Silicon）
        String osArch = System.getProperty("os.arch");
        if ("aarch64".equals(osArch)) {
            return true;
        }

        // 3. 如果是 x86_64 JDK，检查是否正在被 Rosetta 2 转译运行
        try {
            Process process = new ProcessBuilder("sysctl", "-in", "sysctl.proc_translated").start();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String result = reader.readLine();
                process.waitFor();
                // 返回 "1" 表示当前 x86 进程正被 Rosetta 转译，说明底层硬件是 Apple Silicon
                return "1".equals(result);
            }
        } catch (Exception e) {
            // 命令执行失败或不存在该 sysctl 键（旧版 macOS），默认非 Apple Silicon
            return false;
        }
    }

}
