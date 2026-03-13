package com.logan.chatui;

import com.logan.chat.ChatRoleEnum;
import javafx.scene.control.TextArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomepageStyle {

    public static void styleTextArea(TextArea textArea, ChatRoleEnum chatRoleEnum) {
        if (ChatRoleEnum.user.equals(chatRoleEnum)) {
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


    public static void fontSizeTextArea(TextArea textArea, int fontSize) {
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
