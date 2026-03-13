package com.logan.chatui;

import com.logan.chat.SessionCtrl;
import com.logan.chat.llamaccp.LLaMAConf;
import com.logan.config.SysConfigAction;
import com.logan.utils.LogUtils;
import javafx.scene.control.Button;
import javafx.scene.input.Clipboard;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HomepageButtons {


    public static Button getSendButton(){
        Button buttonSend = new Button(SysConfigAction.getLang("send"));
        buttonSend.setPrefWidth(200);
        buttonSend.setStyle("-fx-background-color: #3A5FCD;");

        buttonSend.setOnAction(event -> {
            if (Homepage.isTextAreaInputFreeze) {
                return;
            }
            String msg = Homepage.textAreaInput.getText();
            if (msg == null || msg.isEmpty()) {
                // 输入框为空 → 尝试从剪贴板获取文本
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if (clipboard.hasString()) {           // 优先检查有没有字符串
                    String copyText = clipboard.getString();
                    // 可选：再检查是否为空或只有空白
                    if (copyText != null && !copyText.trim().isEmpty()) {
                        // 这里就是你想要的 copyText
                        msg = copyText;
                    }
                }
            }
            LogUtils.info("======== input msg: " + Homepage.textAreaInput.getText());
            if (msg != null && !msg.isEmpty()) {
                // 消息加入到本地缓存list
                HomepageAdaptor.addQuestion2MessagesList(msg);
                HomepageAdaptor.newThreadAddMessage2Session(msg);
                // 刷新ui，冻结ui不再允许输入
                Homepage.freezeInputTextArea();
                Homepage.freshChatMsgBox();
            }
        });

        return buttonSend;
    }


    public static Button getNewChatButton(){
        Button buttonNewChat = new Button(SysConfigAction.getLang("newChat"));
        buttonNewChat.setOnAction(event -> {
            LogUtils.info("buttonNewChat ");
            if (Homepage.isTextAreaInputFreeze) {
                return;
            }
            SessionCtrl.createSession();
        });

        return buttonNewChat;
    }


    public static Button getOpenDeviceBrowserButton(){
        Button buttonOpenDeviceBrowser = new Button(SysConfigAction.getLang("chatInExplorer"));
        buttonOpenDeviceBrowser.setOnAction(event -> {
            LogUtils.info("buttonOpenDeviceBrowser ");
            try {
                // 需要增加 “http://”
                Desktop.getDesktop().browse(new URI("http://" + LLaMAConf.LLAMA_SERVER_HOST + ":" + LLaMAConf.LLAMA_SERVER_PORT));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        return buttonOpenDeviceBrowser;
    }
}
