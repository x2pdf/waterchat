package com.logan.chatui;

import com.logan.chat.SessionCtrl;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class UIInit {

    public static void initStage(Stage stage) {
        TabPane tabPane = new TabPane();
        Scene scene = new Scene(tabPane);

        AnchorPane homepageAnchorPane = Homepage.getHomeTab();
        // 初始化用戶界面的對話
        SessionCtrl.createSession();
        Homepage.freshChatMsgBox();

        // 组合成分页页面
        Tab tab1 = new Tab(SysConfigAction.getLang("chatWindow"), homepageAnchorPane);
        tab1.setClosable(false);
        tab1.setStyle("-fx-pref-width: 120;");
        HelpPage helpPage = new HelpPage();
        AnchorPane helpAnchorPane = helpPage.getHelpTab();
        Tab tab2 = new Tab(SysConfigAction.getLang("help"), helpAnchorPane);
        tab2.setClosable(false);
        tab2.setStyle("-fx-pref-width: 40;");

        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);

        stage.setScene(scene);
        stage.setMinWidth(300);
        stage.setMinHeight(300);
        stage.setTitle(SysConfig.APP_NAME + " (" + SysConfig.MODEL_NAME + ")");

        stage.getIcons().add(new Image("waterchat_icon.png"));
        stage.show();
    }
}
