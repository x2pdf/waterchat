package com.logan.waterchat.chat.init.helper;

import com.logan.waterchat.config.SysConfigAction;
import com.logan.waterchat.utils.LogUtils;

import java.io.IOException;

public class ModelsExecHelper {

    /**
     * 为可执行文件设置执行权限
     * <p>
     * 当检测到行内容包含"modelsexec"且在macOS系统上运行时，
     * 通过chmod命令为可执行文件添加执行权限。
     *
     * @param line 需要检查的行内容，用于判断是否包含"modelsexec"标识
     * @param fileAbsName 文件绝对路径

     */
    public static void setChomd2ExecFiles(String line, String fileAbsName){
        if (line.contains("modelsexec") && line.endsWith(".gz")) {
            // MacOS 要授权, 才能执行命令行
            if (SysConfigAction.isMacOS()) {
                LogUtils.info("给复制的 modelsexec 文件授权：可执行 chmod +x. file: " + fileAbsName);
                try {
                    Process process = Runtime.getRuntime().exec("chmod +x " + fileAbsName);
                } catch (IOException e) {
                    LogUtils.error("给复制的 modelsexec 文件授权失败: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }


    /**
     * 移除可执行文件的macOS隔离属性
     * <p>
     * 在macOS系统上，对于不包含"modelsexec"标识的文件，
     * 通过xattr命令移除com.apple.quarantine隔离属性，以便文件可以正常执行。
     *
     * @param line 需要检查的行内容，用于判断是否包含"modelsexec"标识
     * @param configTempPath 配置文件临时路径，与fileName拼接形成完整文件路径
     * @param fileName 文件名，与configTempPath拼接形成完整文件路径
     */
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
