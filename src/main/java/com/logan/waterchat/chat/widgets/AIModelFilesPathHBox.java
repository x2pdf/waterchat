package com.logan.waterchat.chat.widgets;


import com.logan.waterchat.chatui.AddAIModelWindow;
import com.logan.waterchat.config.SysConfigAction;
import com.logan.waterchat.utils.AlertUtils;
import com.logan.waterchat.utils.FileUtils;
import com.logan.waterchat.utils.LogUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;

/**
 * author: Logan.qin
 * date: 2022/8/19
 */
public class AIModelFilesPathHBox extends BaseHBox {

    @Override
    public String getHBoxCode() {
        return "AIModelFilesPath";
    }

    @Override
    public AnchorPane initPane() {
        anchorPane = SingleRowAnchorPaneUtils.getTextTextFieldButton(SysConfigAction.getLang("AIModelFilesPath") + ":",
                AddAIModelWindow.aiModelFilesPath, SysConfigAction.getLang("AIModelFilesPathSelectButton"));
        return anchorPane;
    }

    @Override
    public void setAction(Stage stage) {
        Button pathButton = SingleRowAnchorPaneUtils.getButton(anchorPane);
        pathButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String savePath = FileUtils.chooseFilePath(stage);
                if (savePath != null) {
                    savePath = savePath + File.separator;
                    LogUtils.info("savePath refresh: " + savePath);
                    AddAIModelWindow.setAiModelFilesPath(savePath);
                    SingleRowAnchorPaneUtils.getTextFieldAndUpdate(anchorPane, savePath);
                }else {
                    AlertUtils.msg("未选择文件夹。\nNo folder selected.");
                }
            }
        });
    }
}
