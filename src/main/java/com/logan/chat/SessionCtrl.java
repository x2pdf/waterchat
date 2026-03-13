package com.logan.chat;


import com.logan.chatui.HomepageAdaptor;
import com.logan.config.SysConfig;
import com.logan.utils.LocalFileUtils;
import com.logan.utils.LogUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class SessionCtrl {

    public static ArrayList<MessageDTO> messageDTOS = new ArrayList<>();

    public static void createSession() {
        // 检查旧数据，保存
        logSession(messageDTOS);
        // 清除旧数据
        emptySession();

        // 初始化新会话数据
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setRole(ChatRoleEnum.system);
        messageDTO.setContent(SysConfig.MODEL_DEFAULT_SYSTEM_PROMPT);
        messageDTOS.add(messageDTO);

        // 刷新ui
        HomepageAdaptor.freshChatMsgBox();
        HomepageAdaptor.clearInputBox();
    }


    public static void emptySession() {
        messageDTOS.clear();
    }

    public static void logSession(ArrayList<MessageDTO> messageDTOS) {
        if (!SysConfig.IS_LOG_SESSION) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n\n").append("**************************************").append("\n")
                .append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()))
                .append("\n")
                .append(SysConfig.MODEL_NAME)
                .append("\n\n\n");
        for (MessageDTO messageDTO : messageDTOS) {
            stringBuilder.append(messageDTO.getRole()).append(":\n").append(messageDTO.getContent()).append("\n\n\n");
        }
        try {
            LocalFileUtils.appendToMessageFile(stringBuilder.toString(), SysConfig.APP_DOWNLOAD_PATH + SysConfig.SESSION_LOG_FILE_NAME);
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
        return LogUtils.writeArrayListAsString(messageDTOS);
    }



}
