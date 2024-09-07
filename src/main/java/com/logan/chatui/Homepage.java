package com.logan.chatui;

import com.logan.chat.Message;
import com.logan.chat.RoleEnum;
import com.logan.chat.SessionCtrl;
import com.logan.config.SysConfig;
import com.logan.utils.LogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;

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

        Button buttonSend = new Button("发送");
        buttonSend.setPrefWidth(200);
        buttonSend.setStyle("-fx-background-color: #3A5FCD;");

        buttonSend.setOnAction(event -> {
            if (isTextAreaInputFreeze) {
                return;
            }
            String msg = textAreaInput.getText();
            LogUtils.info("======== input msg: " + textAreaInput.getText());
            if (msg != null && !"".equals(msg)) {
                // 消息加入到本地缓存list
                HomepageAdaptor.addQuestion2MessagesList(msg);
                HomepageAdaptor.newThreadAddMessage2Session(msg);
                // 刷新ui，冻结ui不再允许输入
                freezeInputTextArea();
                freshChatMsgBox();
            }
        });

        Button buttonNewChat = new Button("新对话");
        buttonNewChat.setOnAction(event -> {
            LogUtils.info("buttonNewChat ");
            if (isTextAreaInputFreeze) {
                return;
            }
            SessionCtrl.createSession();
        });

        HBox buttonBox = new HBox(buttonNewChat, buttonSend);
        buttonBox.setSpacing(3);
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);

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
            TextArea messageBox = createMessageBox(message.getContent());
            // 重建對話box
            styleTextArea(messageBox, message.getRole());
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
        textAreaInput.setText("正在努力处理您的请求......\n等待中不可再输入文本哦～\n请耐心等待～～～～");
        textAreaInputNoEditable();
        isTextAreaInputFreeze = true;
    }

    public static void unfreezeInputTextArea() {
        textAreaInput.clear();     // clear輸入框内容
        textAreaInputEnableEditable();
        isTextAreaInputFreeze = false;
    }

    private static void styleTextArea(TextArea textArea, RoleEnum roleEnum) {
        if (RoleEnum.system.equals(roleEnum)) {
            textArea.setStyle("-fx-padding: 4; -fx-background-color: #f0f0f0; -fx-border-color: #d3d3d3;");
        } else {
            textArea.setStyle("-fx-padding: 4; -fx-background-color: #C8C8C8; -fx-border-color: #d3d3d3;");
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
        } else {
            row = row + 100;
        }

        int wordsPrefRows = countWordsPrefRows(msgText);
        row = Math.max(wordsPrefRows, row);
//        System.out.println("setTextAreaPrefRow: " + row);
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
