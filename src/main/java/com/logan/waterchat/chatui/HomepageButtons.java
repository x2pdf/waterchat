package com.logan.waterchat.chatui;


import com.logan.waterchat.chat.model.SessionCtrl;
import com.logan.waterchat.chat.llamaccp.LLaMAConf;
import com.logan.waterchat.config.SysConfigAction;
import com.logan.waterchat.utils.LogUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.Clipboard;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HomepageButtons {

    private static final String BROWSER_BUTTON_STYLE =
            "-fx-background-color: linear-gradient(to right, #05A9D6, #5965E8, #C64BCB); " +
                    "-fx-background-radius: 18; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 7 14 7 14;";
    private static final Duration BROWSER_ANIMATION_CYCLE = Duration.millis(5600);
    private static final double BROWSER_BUTTON_SCALE = 1.025;


    public static Button getSendButton(){
        Button buttonSend = new Button(SysConfigAction.getLang("send"));
        buttonSend.setPrefWidth(200);
        buttonSend.setStyle("-fx-background-color: #008B8B; -fx-text-fill: white; -fx-font-weight: bold;");

        buttonSend.setOnAction(event -> {
            if (Homepage.isTextAreaInputFreeze) {
                HomepageAdaptor.cancelCurrentRequest();
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
            if (msg != null) {
                LogUtils.info("input message accepted: chars=" + msg.length()
                        + ", lines=" + com.logan.waterchat.chat.longtext.LongTextUtils.countLines(msg));
            }
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
        styleBrowserButton(buttonOpenDeviceBrowser);
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

    private static void styleBrowserButton(Button button) {
        button.setStyle(BROWSER_BUTTON_STYLE);

        DropShadow animatedGlow = new DropShadow(10, Color.web("#19D4FF", 0.28));
        animatedGlow.setSpread(0.08);

        DropShadow hoverGlow = new DropShadow(15, Color.TRANSPARENT);
        hoverGlow.setSpread(0.18);
        hoverGlow.setInput(animatedGlow);
        button.setEffect(hoverGlow);

        Timeline breathingAnimation = new Timeline(
                browserAnimationFrame(0, button, animatedGlow,
                        Color.web("#19D4FF", 0.28), 10, 0.08, 1.0),
                browserAnimationFrame(850, button, animatedGlow,
                        Color.web("#24D9F3", 0.70), 17, 0.22, BROWSER_BUTTON_SCALE),
                browserAnimationFrame(1700, button, animatedGlow,
                        Color.web("#786BFF", 0.62), 16, 0.19, 1.015),
                browserAnimationFrame(2550, button, animatedGlow,
                        Color.web("#ED5ED9", 0.58), 15, 0.16, 1.01),
                browserAnimationFrame(3200, button, animatedGlow,
                        Color.web("#19D4FF", 0.28), 10, 0.08, 1.0),
                browserAnimationFrame(BROWSER_ANIMATION_CYCLE.toMillis(), button, animatedGlow,
                        Color.web("#19D4FF", 0.28), 10, 0.08, 1.0)
        );
        breathingAnimation.setCycleCount(Animation.INDEFINITE);

        final double[] scaleBeforePress = {1.0, 1.0};
        final boolean[] resumeAfterPress = {false};

        button.setOnMouseEntered(event -> {
            button.setCursor(Cursor.HAND);
            hoverGlow.setColor(Color.web("#FFFFFF", 0.52));
        });
        button.setOnMouseExited(event -> {
            button.setCursor(Cursor.DEFAULT);
            hoverGlow.setColor(Color.TRANSPARENT);
        });
        button.setOnMousePressed(event -> {
            resumeAfterPress[0] = breathingAnimation.getStatus() == Animation.Status.RUNNING;
            breathingAnimation.pause();
            scaleBeforePress[0] = button.getScaleX();
            scaleBeforePress[1] = button.getScaleY();
            button.setScaleX(0.98);
            button.setScaleY(0.98);
        });
        button.setOnMouseReleased(event -> {
            button.setScaleX(scaleBeforePress[0]);
            button.setScaleY(scaleBeforePress[1]);
            if (resumeAfterPress[0] && button.getScene() != null) {
                breathingAnimation.play();
            }
        });

        button.sceneProperty().addListener((observable, previousScene, currentScene) -> {
            if (currentScene == null) {
                breathingAnimation.pause();
            } else {
                breathingAnimation.play();
            }
        });
    }

    private static KeyFrame browserAnimationFrame(double millis, Button button, DropShadow glow,
                                                   Color color, double radius, double spread, double scale) {
        return new KeyFrame(Duration.millis(millis),
                new KeyValue(glow.colorProperty(), color),
                new KeyValue(glow.radiusProperty(), radius),
                new KeyValue(glow.spreadProperty(), spread),
                new KeyValue(button.scaleXProperty(), scale),
                new KeyValue(button.scaleYProperty(), scale));
    }
}
