package com.logan.chat;


import com.logan.chatui.HomepageAdaptor;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;
import com.logan.utils.LocalFileUtils;
import com.logan.utils.LogUtils;

import java.io.IOException;
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
        message.setContent(SysConfig.MODEL_DEFAULT_SYSTEM_PROMPT);
        messages.add(message);

        // 刷新ui
        HomepageAdaptor.freshChatMsgBox();
        HomepageAdaptor.clearInputBox();
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
                .append("\n")
                .append(SysConfig.MODEL_NAME)
                .append("\n\n\n");
        for (Message message : messages) {
            stringBuilder.append(message.getRole()).append(":\n").append(message.getContent()).append("\n\n\n");
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



}
