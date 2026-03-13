package com.logan.chatui;

import com.logan.chat.MessageDTO;
import com.logan.chat.llamaccp.LLaMAServerCtrl;
import com.logan.chat.ChatRoleEnum;
import com.logan.chat.SessionCtrl;
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
    /**
     * 异步调用AddMessage2Session
     *
     * @param msg
     */
    public static void newThreadAddMessage2Session(String msg) {
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        // 模拟耗时操作
                        Thread.sleep(30);
                        return LLaMAServerCtrl.callLLaMAServer();
                    }
                };
            }
        };

        // 监听Service的状态，当任务完成时更新TextArea
        service.valueProperty().addListener((observable, oldValue, newValue) -> {
            // 将ai回答消息加入到本地缓存list
            HomepageAdaptor.addAnswer2MessagesList(newValue);
            // 刷新ui
            HomepageAdaptor.freshChatMsgBox();
            Homepage.unfreezeInputTextArea();
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
            if (HomepageAdaptor.ENABLE_THINKING){
                msg += HomepageAdaptor.THINKING_TEXT;
            }else {
                msg += HomepageAdaptor.NO_THINKING_TEXT;
            }
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
