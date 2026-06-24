package com.logan.waterchat.chat.init.helper;

import com.logan.waterchat.config.SysConfigAction;
import com.logan.waterchat.utils.LogUtils;

import java.io.IOException;

public class ModelsExecHelper {

    public static void setChomd2ExecFiles(String line, String configTempPath, String fileName){
        if (line.contains("modelsexec")) {
            // MacOS 要授权, 才能执行命令行
            if (SysConfigAction.isMacOS()) {
                LogUtils.info("给复制的 modelsexec 文件授权：可执行 chmod +x. file: " + configTempPath + fileName);
                try {
                    Process process = Runtime.getRuntime().exec("chmod +x " + configTempPath + fileName);
                } catch (IOException e) {
                    LogUtils.error("给复制的 modelsexec 文件授权失败: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void removeQuarantine2ExecFiles(String line, String configTempPath, String fileName){
        if (SysConfigAction.isMacOS() && !line.contains("modelsexec")) {
            try {
                ProcessBuilder xattrProcess = new ProcessBuilder(
                        "xattr", "-d", "com.apple.quarantine", configTempPath + fileName
                );
                xattrProcess.start().waitFor();
            } catch (Exception e) {
                LogUtils.error("移除 quarantine 失败: " + e.getMessage());
            }
        }
    }
}
