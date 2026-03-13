package com.logan;


import com.logan.chat.llamaccp.LLaMAServerCtrl;
import com.logan.chat.refresh.RefreshConfig;
import com.logan.chatui.UIInit;
import com.logan.config.InitSourceTemplate;
import com.logan.utils.LogUtils;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


/**
 * JavaFX App
 */
// Tag 24.01
public class App extends Application {
    public static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage) {
        try {
            // 根据app本地文件夹中是否存在 resources/modelsexec 判断是否需要初始化文件资源
            if (InitSourceTemplate.isNeedInitResources()) {
                InitSourceTemplate initSourceTemplate = new InitSourceTemplate();
                initSourceTemplate.init();
            }
            RefreshConfig.refreshConfig();
            LLaMAServerCtrl.startLLaMAServer();

            UIInit.initStage(stage);
            primaryStage = stage;
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    LLaMAServerCtrl.shutdownLLaMAServer();
                    LogUtils.trimLogFileIfTooLarge();
                    System.gc();
                }
            });
        } catch (Exception e) {
            LogUtils.error("initConfig exception: " + e.toString());
        } catch (Error error) {
            LogUtils.error("App Error. error info:" + error);
            LLaMAServerCtrl.shutdownLLaMAServer();
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setTitle("ERROR");
            warning.setContentText("The program runs wrongly, sorry!");
            warning.showAndWait();
        }
    }

}
