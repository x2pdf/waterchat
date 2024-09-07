package com.logan.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class AlertUtils {

    public static void openExplorer(String savePath) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Info");
        alert.setContentText("文件保存在：" + "\n" + savePath);
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isPresent()) {
            if (buttonType.get() == ButtonType.CANCEL) {
                return;
            }
            try {
//                        Runtime.getRuntime().exec("explorer " + savePath);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(new File(savePath));
                }
                System.gc();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void msg(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Info");
        alert.setContentText(msg);
        Optional<ButtonType> buttonType = alert.showAndWait();
    }


    public static void saveProductIntroduction(String savePath) {
        try {
            String fileFullName = "waterchat_product_introduction_cn.pdf";
            LocalFileUtils.save2TempDir(LocalFileUtils.is2Byte(Objects.requireNonNull(AlertUtils.class.getClassLoader()
                            .getResourceAsStream("asset/" + fileFullName))),
                    savePath, fileFullName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
