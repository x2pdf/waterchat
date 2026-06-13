package com.logan.waterchat.chatui;


import com.logan.waterchat.chat.ChatRoleEnum;
import com.logan.waterchat.chat.MessageDTO;
import com.logan.waterchat.chat.SessionCtrl;
import com.logan.waterchat.chat.llamaccp.LLaMAServerCtrl;
import com.logan.waterchat.utils.LogUtils;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomepageAdaptor {

    public static boolean ENABLE_THINKING = false;
    public static String THINKING_TEXT = " /think";
    public static String NO_THINKING_TEXT = " /no_think";
    public static Boolean IS_NEED_SYSTEM_PROMPT = true;
    // 在类里加一个静态字段
    private static Service<String> service;

    /**
     * 异步调用AddMessage2Session
     *
     * @param msg
     */
    public static void newThreadAddMessage2Session(String msg) {
        // 先取消上一个（防止重复）
        if (service != null && service.isRunning()) {
            service.cancel();
        }
        service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        try {
                            String result = LLaMAServerCtrl.callLLaMAServer();
                            LogUtils.info("======== AI answer1: " + result);
                            return result;
                        } catch (Exception e) {
                            LogUtils.error("Task 执行异常" + e);
                            throw e;   // 让它走 FAILED 流程
                        }
                    }
                };
            }
        };

        // 监听Service的状态，当任务完成时更新TextArea
        service.setOnSucceeded(event -> {
            try {
                String result = service.getValue();
                LogUtils.info("======== AI answer2: " + result);

                HomepageAdaptor.addAnswer2MessagesList(result);
                HomepageAdaptor.freshChatMsgBox();
                Homepage.unfreezeInputTextArea();

                LogUtils.info("======== AI answer2 处理完成");
            } catch (Exception e) {
                LogUtils.error("setOnSucceeded 中发生异常" + e);
                e.printStackTrace();
            }
        });
        service.start();

    }

    public static void clearInputBox() {
        Homepage.textAreaInput.clear();
    }

    public static void freshChatMsgBox() {
        Homepage.freshChatMsgBox();
    }

    public static void textAreaInputNoEditable() {
        // 设置文本不可编辑
        Homepage.textAreaInputNoEditable();
    }

    public static void textAreaInputEnableEditable() {
        // 设置文本不可编辑
        Homepage.textAreaInputEnableEditable();
    }

    public static void addQuestion2MessagesList(String msg) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setRole(ChatRoleEnum.user);
        messageDTO.setContent(msg);
        SessionCtrl.messageDTOS.add(messageDTO);
    }

    public static void addAnswer2MessagesList(String msg) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setRole(ChatRoleEnum.assistant);
        messageDTO.setContent(msg);
        SessionCtrl.messageDTOS.add(messageDTO);
    }

    public static void addSystemFakeMsg() {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setRole(ChatRoleEnum.assistant);
        messageDTO.setContent("****\n正在努力处理您的请求......\n等待中不可再输入文本哦～\n请耐心等待～～～～");
        SessionCtrl.messageDTOS.add(messageDTO);
    }

    public static void popupSystemFakeMsg() {
        for (int i = SessionCtrl.messageDTOS.size() - 1; i > 0; i--) {
            MessageDTO messageDTO = SessionCtrl.messageDTOS.get(i);
            if (messageDTO.getContent().startsWith("****")) {
                SessionCtrl.messageDTOS.remove(i);
            }
        }
    }

    public static List<Map<String, String>> assembleMsg(List<Map<String, String>> messages) {
        for (MessageDTO messageDTO : SessionCtrl.messageDTOS) {
            String msg = messageDTO.getContent();
//            if (HomepageAdaptor.ENABLE_THINKING){
//                msg += HomepageAdaptor.THINKING_TEXT;
//            }else {
//                msg += HomepageAdaptor.NO_THINKING_TEXT;
//            }
            messages.add(msg(messageDTO.getRole().toString(), msg));
        }
        return messages;
    }

    private static Map<String, String> msg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

}
