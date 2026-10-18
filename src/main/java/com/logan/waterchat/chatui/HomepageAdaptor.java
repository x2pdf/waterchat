package com.logan.waterchat.chatui;


import com.logan.waterchat.chat.model.ChatRoleEnum;
import com.logan.waterchat.chat.model.MessageDTO;
import com.logan.waterchat.chat.model.SessionCtrl;
import com.logan.waterchat.chat.llamaccp.LLaMAServerCtrl;
import com.logan.waterchat.chat.longtext.LongTextUtils;
import com.logan.waterchat.chat.longtext.LongTextTooLargeException;
import com.logan.waterchat.utils.LogUtils;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomepageAdaptor {

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
        Service<String> currentService = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        try {
                            String preview = LongTextUtils.createPreview(msg);
                            Platform.runLater(() -> {
                                updateLatestUserDisplay(preview);
                                HomepageAdaptor.freshChatMsgBox();
                            });
                            return LLaMAServerCtrl.callLLaMAServer(msg, this::updateMessage,
                                    this::isCancelled);
                        } catch (Exception e) {
                            LogUtils.error("Task 执行异常" + e);
                            throw e;   // 让它走 FAILED 流程
                        }
                    }
                };
            }
        };
        service = currentService;
        currentService.messageProperty().addListener((observable, oldValue, newValue) ->
                Homepage.setProgressText(newValue));

        // 监听Service的状态，当任务完成时更新TextArea
        currentService.setOnSucceeded(event -> {
            try {
                String result = currentService.getValue();
                HomepageAdaptor.addAnswer2MessagesList(result);
                HomepageAdaptor.freshChatMsgBox();
            } catch (Exception e) {
                LogUtils.error("setOnSucceeded 中发生异常" + e);
                e.printStackTrace();
            } finally {
                Homepage.unfreezeInputTextArea();
            }
        });
        currentService.setOnFailed(event -> {
            Throwable error = currentService.getException();
            String errorMessage = error == null ? "未知错误" : error.toString();
            LogUtils.error("AI 请求失败: " + errorMessage);
            LogUtils.log2LocalLogFile("AI 请求失败: " + errorMessage);
            Homepage.unfreezeInputTextArea();
            if (error instanceof LongTextTooLargeException) {
                AlertUtils.msg(error.getMessage());
            } else {
                AlertUtils.msg("AI 请求失败，请确认本地模型服务正常运行。\n\n" + errorMessage);
            }
        });
        currentService.setOnCancelled(event -> {
            LogUtils.info("AI 请求已取消");
            Homepage.unfreezeInputTextArea();
        });
        currentService.start();

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
        messageDTO.setDisplayContent(LongTextUtils.preparingDisplay(msg));
        messageDTO.setModelContent(msg);
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
            String msg = messageDTO.getModelContent();
            messages.add(msg(messageDTO.getRole().toString(), msg));
        }
        return messages;
    }

    public static void cancelCurrentRequest() {
        Service<String> current = service;
        if (current != null && current.isRunning()) {
            current.cancel();
        }
    }

    public static void updateLatestUserDisplay(String display) {
        for (int i = SessionCtrl.messageDTOS.size() - 1; i >= 0; i--) {
            MessageDTO message = SessionCtrl.messageDTOS.get(i);
            if (message.getRole() == ChatRoleEnum.user) {
                message.setDisplayContent(display);
                return;
            }
        }
    }

    public static void compressLatestUserMessageForModel(String raw) {
        for (int i = SessionCtrl.messageDTOS.size() - 1; i >= 0; i--) {
            MessageDTO message = SessionCtrl.messageDTOS.get(i);
            if (message.getRole() == ChatRoleEnum.user && raw.equals(message.getContent())) {
                message.setModelContent("[一份长文本已通过分块流程分析："
                        + LongTextUtils.countLines(raw) + " 行，" + raw.length()
                        + " 字符。完整内容未重复放入后续上下文。]");
                return;
            }
        }
    }

    private static Map<String, String> msg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

}
