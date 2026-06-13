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

/**
 * author: Logan.qin
 * date: 2022/8/19
 */
public class AIModelMainFilePathHBox extends BaseHBox {

    @Override
    public String getHBoxCode() {
        return "AIModelMainFilePath";
    }

    @Override
    public AnchorPane initPane() {
        anchorPane = SingleRowAnchorPaneUtils.getTextTextFieldButton(SysConfigAction.getLang("AIModelMainFile") + ":",
                AddAIModelWindow.aiModelMainFile, SysConfigAction.getLang("AIModelMainFileSelectButton"));
        return anchorPane;
    }

    @Override
    public void setAction(Stage stage) {
        Button pathButton = SingleRowAnchorPaneUtils.getButton(anchorPane);
        pathButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String chooseFile = FileUtils.chooseFile(stage);
                if (chooseFile != null) {
                    LogUtils.info("chooseFile: " + chooseFile);
                    AddAIModelWindow.setAiModelMainFile(chooseFile);
                    SingleRowAnchorPaneUtils.getTextFieldAndUpdate(anchorPane, chooseFile);
                }else {
                    AlertUtils.msg("未选择文件。\nNo file selected.");
                }
            }
        });
    }
}
