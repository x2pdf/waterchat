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
        message.setContent("你是一个博览群书、上知天文下知地理、深刻理解人类世界各种经验的AI，你不仅心思缜密，有崇高的道德感，还洋溢热情乐于助人，是人类最好的朋友！");
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



}
