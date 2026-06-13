package com.logan.waterchat.chatui;


import com.logan.waterchat.chat.ChatRoleEnum;
import com.logan.waterchat.chat.MessageDTO;
import com.logan.waterchat.chat.SessionCtrl;
import com.logan.waterchat.config.SysConfig;
import com.logan.waterchat.utils.LogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;

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

        Button sendButton = HomepageButtons.getSendButton();
        Button newChatButton = HomepageButtons.getNewChatButton();
        Button openDeviceBrowserButton = HomepageButtons.getOpenDeviceBrowserButton();

        CheckBox isNeedSystemPrompt = HomepageCheckBox.getIsNeedSystemPrompt();
        CheckBox enableThinking = HomepageCheckBox.getEnableThinking();

        // 创建占位 Region 来把右边按钮推到右边
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBox = new HBox(openDeviceBrowserButton, spacer, isNeedSystemPrompt, enableThinking, newChatButton, sendButton);
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
        int msgSize = SessionCtrl.messageDTOS.size();
        for (int i = 0; i < msgSize; i++) {
            // 獲取新的所有消息
            MessageDTO messageDTO = SessionCtrl.messageDTOS.get(i);
            // 移除系统提示语
            if (!HomepageAdaptor.IS_NEED_SYSTEM_PROMPT && messageDTO.getRole().equals(ChatRoleEnum.system)) {
                continue;
            }
            TextArea messageBox = createMessageBox(messageDTO.getContent());
            // 重建對話box
            HomepageStyle.styleTextArea(messageBox, messageDTO.getRole());
            HomepageStyle.fontSizeTextArea(messageBox, SysConfig.FONT_SIZE);
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
        textArea.setPrefRowCount(HomepageStyle.setTextAreaPrefRow(msgText));
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
            textAreaInput.setPrefRowCount(HomepageStyle.setTextAreaPrefRow(newValue));
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

}
