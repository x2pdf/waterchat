package com.logan.chat;


import com.logan.chatui.HomepageAdaptor;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;
import com.logan.utils.LocalFileUtils;
import com.logan.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class SessionCtrl {

    public static ArrayList<Message> messages = new ArrayList<>();

    public static void createSession() {
        // 检查旧数据，保存
        logSession(messages);
        // 清除旧数据
        emptySession();

        // 初始化新会话数据
        Message message = new Message();
        message.setRole(RoleEnum.system);
        message.setContent("How can I help you today?");
        messages.add(message);

        // 刷新ui
        HomepageAdaptor.freshChatMsgBox();
        HomepageAdaptor.clearInputBox();
    }


    public static String addMessage2Session(String msg) throws IOException {
        // 调用python model api
        String messagesJson = getMessageStr();
        byte[] msgBytes = new byte[1024];
        if (messagesJson != null) {
            msgBytes = messagesJson.getBytes(StandardCharsets.UTF_8);
        }
        LocalFileUtils.save2Path(msgBytes, SysConfig.APP_DOWNLOAD_PATH, SysConfig.MESSAGE_FILENAME);
        LogUtils.info("messages file: " + SysConfig.APP_DOWNLOAD_PATH + SysConfig.MESSAGE_FILENAME);

        callPython(SysConfig.APP_DOWNLOAD_PATH + SysConfig.MESSAGE_FILENAME,
                SysConfig.TEMP_RESOURCES_PATH + SysConfig.CONFIG_PATH,
                SysConfig.APP_DOWNLOAD_PATH + SysConfig.RESPONSE_FILENAME);
        byte[] responseBytes = LocalFileUtils.load(SysConfig.APP_DOWNLOAD_PATH + SysConfig.RESPONSE_FILENAME);
        String response = new String(responseBytes, StandardCharsets.UTF_8);

        return response;
    }

    public static void emptySession() {
        messages.clear();
    }

    public static void logSession(ArrayList<Message> messages) {
        if (!SysConfig.IS_LOG_SESSION) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n\n").append("**************************************").append("\n")
                .append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()))
                .append("\n\n");
        for (Message message : messages) {
            stringBuilder.append(message.getRole()).append(":  ").append(message.getContent()).append("\n");
        }
        try {
            LocalFileUtils.appendToMessageFile(stringBuilder.toString(), SysConfigAction.createAppLocalPath() + SysConfig.SESSION_LOG_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.info("messages log file: " + SysConfig.APP_DOWNLOAD_PATH + SysConfig.SESSION_LOG_FILE_NAME);
    }

    /**
     * 获取AI模型请求模版 tokenizer.apply_chat_template() 的字符串
     *
     * @return
     */
    public static String getMessageStr() {
        return LogUtils.writeArrayListAsString(messages);
    }

    /**
     * 调用本地可执行的python文件
     *
     * @param messagePath  消息文件路径，必传参数
     * @param configPath   配置文件路径，可选参数
     * @param responsePath 返回消息文件保存路径，可选参数
     */
    public static void callPython(String messagePath, String configPath, String responsePath) {
        try {
            if (messagePath == null || "".equals(messagePath.trim())) {
                return;
            }
            long start = System.currentTimeMillis();
            String command = SysConfig.TEMP_RESOURCES_PATH + SysConfigAction.getModelExecSourcePath();

            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                command = command + " \"" + messagePath + "\"";
                if (configPath != null) {
                    command = command + " \"" + configPath + "\"";
                }
                if (responsePath != null) {
                    command = command + " \"" + responsePath + "\"";
                }
            } else {
                // mac 有双引号""包括参数反而报路径不存在 '"/User/...."'
                command = command + " " + messagePath + "";
                if (configPath != null) {
                    command = command + " " + configPath + "";
                }
                if (responsePath != null) {
                    command = command + " " + responsePath + "";
                }
            }



            LogUtils.info("command: " + command);
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                LogUtils.info(line);
            }
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                LogUtils.error("errorLine: " + errorLine); // Python脚本的错误输出
            }

            int exitValue = process.waitFor(); // success:1, fail:0
            if (exitValue == 1) {
                LogUtils.info("call python success.");
            } else {
                LogUtils.info("call python fail.");
            }

            LogUtils.info("===== call python spend(s): " + (System.currentTimeMillis() - start) / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
