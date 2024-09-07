package com.logan;


import com.logan.chat.SessionCtrl;
import com.logan.chatui.HelpPage;
import com.logan.chatui.Homepage;
import com.logan.config.InitSource;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;
import com.logan.utils.LogUtils;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


/**
 * JavaFX App
 */

public class App extends Application {
    private final static String statement = "Please use it for learning purposes only.  --author Logan Qin\n"
            + "Thank you for your understanding and cooperation.\n"
            + "All Rights Reserved.\n";
    private static Scene scene;
    public static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            SysConfigAction.initSysConfigValue();
            // 根据app本地文件夹中是否存在 resources/modelsexec 判断是否需要初始化文件资源
            if (SysConfigAction.isNeedInitResources()) {
                InitSource initSource = new InitSource();
                initSource.init();
            }

            SysConfigAction.refreshConfig();
            initStage(stage);
            primaryStage = stage;
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.gc();
                }
            });
        } catch (Exception e) {
            LogUtils.error("initConfig exception: " + e.toString());
        } catch (Error error) {
            LogUtils.error("App Error. error info:" + error);
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setTitle("ERROR");
            warning.setContentText("The program runs wrongly, sorry!");
            warning.showAndWait();
        }
    }


    public void initStage(Stage stage) {
        TabPane tabPane = new TabPane();
        scene = new Scene(tabPane);

        AnchorPane homepageAnchorPane = Homepage.getHomeTab();
        // 初始化用戶界面的對話
        SessionCtrl.createSession();
        Homepage.freshChatMsgBox();

        // 组合成分页页面
        Tab tab1 = new Tab("会话窗口", homepageAnchorPane);
        tab1.setClosable(false);
        tab1.setStyle("-fx-pref-width: 80;");

        AnchorPane helpAnchorPane = HelpPage.getHelpTab();
        Tab tab2 = new Tab("Help", helpAnchorPane);
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

    /**
     * 刷新应用名称
     */
    public static void updateAppName() {
        if (primaryStage != null) {
            primaryStage.setTitle(SysConfig.APP_NAME + " (" + SysConfig.MODEL_NAME + ")");
        }
    }

}
