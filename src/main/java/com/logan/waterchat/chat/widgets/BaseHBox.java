package com.logan.waterchat.chat.widgets;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * author: Logan.qin
 * date: 2022/8/19
 */

public abstract class BaseHBox extends HBox {

    public AnchorPane anchorPane;

    public abstract String getHBoxCode();

    public abstract AnchorPane initPane();

    public abstract void setAction(Stage stage);

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public void setAnchorPane(AnchorPane anchorPane) {
        this.anchorPane = anchorPane;
    }
}
