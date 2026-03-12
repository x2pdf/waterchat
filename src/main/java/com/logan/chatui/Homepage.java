package com.logan.chatui;

import com.logan.chat.LLaMAConf;
import com.logan.chat.Message;
import com.logan.chat.RoleEnum;
import com.logan.chat.SessionCtrl;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;
import com.logan.utils.LogUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Homepage {
    public static TextArea textAreaInput = new TextArea();
    public static VBox vbox = new VBox();
    public static ScrollPane scrollPane = new ScrollPane(vbox);
    public static boolean isTextAreaInputFreeze = false;


    public static AnchorPane getHomeTab() {
        AnchorPane homepageAnchorPane = new AnchorPane();
        homepageAnchorPane.setPrefSize(SysConfig.STAGE_WIDTH, SysConfig.STAGE_HEIGHT);

        // 设置文本输入框
        setInputTextArea();

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.BOTTOM_CENTER);
        stackPane.getChildren().add(textAreaInput);

        Button buttonSend = new Button(SysConfigAction.getLang("send"));
        buttonSend.setPrefWidth(200);
        buttonSend.setStyle("-fx-background-color: #3A5FCD;");

        buttonSend.setOnAction(event -> {
            if (isTextAreaInputFreeze) {
                return;
            }
            String msg = textAreaInput.getText();
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
            LogUtils.info("======== input msg: " + textAreaInput.getText());
            if (msg != null && !msg.isEmpty()) {
                // 消息加入到本地缓存list
                HomepageAdaptor.addQuestion2MessagesList(msg);
                HomepageAdaptor.newThreadAddMessage2Session(msg);
                // 刷新ui，冻结ui不再允许输入
                freezeInputTextArea();
                freshChatMsgBox();
            }
        });

        Button buttonNewChat = new Button(SysConfigAction.getLang("newChat"));
        buttonNewChat.setOnAction(event -> {
            LogUtils.info("buttonNewChat ");
            if (isTextAreaInputFreeze) {
                return;
            }
            SessionCtrl.createSession();
        });


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

        CheckBox isNeedSystemPrompt = new CheckBox(SysConfigAction.getLang("isNeedSystemPrompt"));
        isNeedSystemPrompt.setSelected(true);   // 这句让它默认勾选
        isNeedSystemPrompt.setOnAction(e -> {
            if (isNeedSystemPrompt.isSelected()) {
                HomepageAdaptor.IS_NEED_SYSTEM_PROMPT = true;
                boolean isHasSystemPrompt = false;
                for (Message message : SessionCtrl.messages) {
                    if (message.getRole().equals(RoleEnum.system)) {
                        isHasSystemPrompt = true;
                    }
                }
                if (!isHasSystemPrompt) {
                    Message message = new Message();
                    message.setRole(RoleEnum.system);
                    message.setContent(SysConfig.MODEL_DEFAULT_SYSTEM_PROMPT);
                    SessionCtrl.messages.add(0, message);
                }
                LogUtils.info("isNeedSystemPrompt 功能已启用");
            } else {
                HomepageAdaptor.IS_NEED_SYSTEM_PROMPT = false;
                Message message = SessionCtrl.messages.get(0);
                if (message.getRole().equals(RoleEnum.system)) {
                    SessionCtrl.messages.remove(0);
                }
                LogUtils.info("isNeedSystemPrompt 功能已关闭");
            }

            HomepageAdaptor.freshChatMsgBox();
        });

        CheckBox enableThinking = new CheckBox(SysConfigAction.getLang("thinkingMode"));
        enableThinking.setOnAction(e -> {
            if (enableThinking.isSelected()) {
                HomepageAdaptor.ENABLE_THINKING = true;
                LogUtils.info("Thinking 功能已启用");
            } else {
                HomepageAdaptor.ENABLE_THINKING = false;
                LogUtils.info("Thinking 功能已关闭");
            }
        });

        // 创建占位 Region 来把右边按钮推到右边
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox buttonBox = new HBox(buttonOpenDeviceBrowser, spacer, isNeedSystemPrompt, enableThinking, buttonNewChat, buttonSend);
        buttonBox.setSpacing(5);
        buttonBox.setAlignment(Pos.BASELINE_CENTER);

        VBox homepageVBox = new VBox();
        homepageVBox.setVgrow(scrollPane, Priority.ALWAYS); // 将 scrollPane 中的会话窗口从最顶开始往下显示
        homepageVBox.getChildren().addAll(scrollPane, stackPane, buttonBox);
        homepageVBox.setSpacing(3);
        homepageVBox.setPadding(new Insets(SysConfig.MARGIN_DEFAULT, 2, SysConfig.MARGIN_DEFAULT, 2));
        homepageVBox.setAlignment(Pos.BOTTOM_CENTER);

        AnchorPane.setTopAnchor(homepageVBox, SysConfig.MARGIN_DEFAULT);
        AnchorPane.setLeftAnchor(homepageVBox, SysConfig.MARGIN_DEFAULT);
        AnchorPane.setRightAnchor(homepageVBox, SysConfig.MARGIN_DEFAULT);
        AnchorPane.setBottomAnchor(homepageVBox, SysConfig.MARGIN_DEFAULT);

        homepageAnchorPane.getChildren().addAll(homepageVBox);
        return homepageAnchorPane;
    }


    public static void freshChatMsgBox() {
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setSpacing(SysConfig.MARGIN_DEFAULT);

        // 先清除已有消息
        vbox.getChildren().clear();
        int msgSize = SessionCtrl.messages.size();
        for (int i = 0; i < msgSize; i++) {
            // 獲取新的所有消息
            Message message = SessionCtrl.messages.get(i);
            // 移除系统提示语
            if (!HomepageAdaptor.IS_NEED_SYSTEM_PROMPT && message.getRole().equals(RoleEnum.system)) {
                continue;
            }
            TextArea messageBox = createMessageBox(message.getContent());
            // 重建對話box
            styleTextArea(messageBox, message.getRole());
            fontSizeTextArea(messageBox, SysConfig.FONT_SIZE);
            vbox.getChildren().add(messageBox);
        }

        scrollPane.setFitToWidth(true);
        for (int i = 0; i < 30; i++) {
            // 调用一次通常不会到滑到最底部，所以。。。
            scrollPane.setVvalue(1); // 设置滚动条位置到最底部
        }
        LogUtils.info("freshChatMsgBox success.");
    }

    public static void textAreaInputNoEditable() {
        // 设置文本不可编辑
        textAreaInput.setEditable(false);
    }

    public static void textAreaInputEnableEditable() {
        // 设置文本不可编辑
        textAreaInput.setEditable(true);
    }

    private static TextArea createMessageBox(String msgText) {
        TextArea textArea = new TextArea();
        // 设置文本内容
        textArea.setText(msgText);
        // 设置自动换行
        textArea.setWrapText(true);
        // 设置文本不可编辑
        textArea.setEditable(false);
        // 设置文本行数
        textArea.setPrefRowCount(setTextAreaPrefRow(msgText));
        return textArea;
    }

    private static void setInputTextArea() {
        // 初始化TextArea的高度
        textAreaInput.setPrefRowCount(SysConfig.TEXT_AREA_INPUT_BOX_ROW);
        textAreaInput.setStyle("-fx-border-color: tomato");
        // 设置自动换行
        textAreaInput.setWrapText(true);
        textAreaInput.textProperty().addListener((observable, oldValue, newValue) -> {
            // 在这里处理文本变化事件
            textAreaInput.setPrefRowCount(setTextAreaPrefRow(newValue));
        });
    }

    public static void freezeInputTextArea() {
        textAreaInput.clear();     // clear輸入框内容
        textAreaInput.setText(getFreezeInputTextByLanguage(SysConfig.LANG));
        textAreaInputNoEditable();
        isTextAreaInputFreeze = true;
    }

    public static String getFreezeInputTextByLanguage(String lang) {
        if ("cn".equals(lang)) {
            return "正在努力处理您的请求......\n等待中不可再输入文本哦～\n请耐心等待～～～～";
        } else {
            return "Your request is being processed... You cannot enter text while waiting. Please wait patiently.";
        }
    }

    public static void unfreezeInputTextArea() {
        textAreaInput.clear();     // clear輸入框内容
        textAreaInputEnableEditable();
        isTextAreaInputFreeze = false;
    }

    private static void styleTextArea(TextArea textArea, RoleEnum roleEnum) {
        if (RoleEnum.user.equals(roleEnum)) {
            textArea.setStyle(
                    "-fx-background-color: #f0f0f0; " +
                            "-fx-control-inner-background: #DCDCDC;" +
                            "-fx-background-insets: 0; " +
                            "-fx-background-radius: 0; " +
                            "-fx-padding: 2; " +
                            "-fx-border-color: #d3d3d3;"
            );
        } else {
            textArea.setStyle(
                    "-fx-background-color: #f0f0f0; " +
                            "-fx-background-insets: 0; " +
                            "-fx-background-radius: 0; " +
                            "-fx-padding: 2; " +
                            "-fx-border-color: #d3d3d3;"
            );
        }
    }


    private static void fontSizeTextArea(TextArea textArea, int fontSize) {
        String currentStyle = textArea.getStyle();
        if (fontSize > 0) {
            textArea.setStyle(currentStyle + " -fx-font-size: " + String.valueOf(fontSize) + "px;");
        } else {
            textArea.setStyle(currentStyle + " -fx-font-size: 16px;");
        }
    }

    public static int setTextAreaPrefRow(String msgText) {
        int row = 2;
        int lines = countLines(msgText);
        if (lines < 6) {
            row = row + 2;
        } else if (lines < 10) {
            row = row + 6;
        } else if (lines < 50) {
            row = row + 20;
        } else if (lines < 200) {
            row = row + 50;
        } else if (lines < 400) {
            row = row + 80;
        } else if (lines < 800) {
            row = row + 120;
        } else if (lines < 1200) {
            row = row + 200;
        } else {
            row = row + 300;
        }

        int wordsPrefRows = countWordsPrefRows(msgText);
        row = Math.max(wordsPrefRows, row);
        return row;
    }

    public static int countLines(String str) {
        Pattern pattern = Pattern.compile("\\r\\n|\\r|\\n");
        Matcher matcher = pattern.matcher(str);
        int count = 1; // 初始为1，因为第一行没有换行符
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public static int countWordsPrefRows(String str) {
        // 800像素宽度时，假设 40 字为一行
        return str.length() / 40;
    }

}
