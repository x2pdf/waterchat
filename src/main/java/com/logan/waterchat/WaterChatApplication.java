package com.logan.waterchat;

import com.logan.waterchat.chat.llamaccp.LLaMAServerCtrl;
import com.logan.waterchat.chat.refresh.RefreshConfig;
import com.logan.waterchat.chatui.UIInit;
import com.logan.waterchat.config.Heartbeat;
import com.logan.waterchat.config.InitSourceTemplate;
import com.logan.waterchat.utils.LogUtils;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class WaterChatApplication extends Application {
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
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
            Heartbeat.start();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    LLaMAServerCtrl.shutdownLLaMAServer();
                    LogUtils.trimLogFileIfTooLarge();
                    Heartbeat.stop();
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
