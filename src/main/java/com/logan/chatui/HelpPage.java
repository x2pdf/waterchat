package com.logan.chatui;

import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;
import com.logan.utils.AlertUtils;
import com.logan.utils.LogUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.ArrayList;

public class HelpPage {

    public static AnchorPane getHelpTab() {

        HBox boxHelp = getBoxHelp();
        boxHelp.setAlignment(Pos.TOP_CENTER);

        VBox helpVBox = new VBox();
        helpVBox.getChildren().addAll(boxHelp);
        helpVBox.setSpacing(10);
        helpVBox.setPadding(new Insets(SysConfig.MARGIN_DEFAULT, 0, SysConfig.MARGIN_DEFAULT, 0));
        helpVBox.setAlignment(Pos.BASELINE_CENTER);
        AnchorPane.setTopAnchor(helpVBox, SysConfig.MARGIN_DEFAULT);
        AnchorPane.setLeftAnchor(helpVBox, SysConfig.MARGIN_DEFAULT);
        AnchorPane.setRightAnchor(helpVBox, SysConfig.MARGIN_DEFAULT);
        AnchorPane.setBottomAnchor(helpVBox, SysConfig.MARGIN_DEFAULT);

        AnchorPane helpAnchorPane = new AnchorPane();
        helpAnchorPane.setPrefSize(SysConfig.STAGE_WIDTH, SysConfig.STAGE_HEIGHT);
        helpAnchorPane.getChildren().add(helpVBox);

        return helpAnchorPane;
    }

    public static HBox getBoxHelp() {


        ChoiceBox modelsChoiceBox = getModelsChoiceBox();
        ChoiceBox logChoiceBox = getLogChoiceBox();

        Button tipsButton = new Button("使用建议");
        tipsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AlertUtils.msg(getTipsMsg());
            }
        });

        Button productIntroductionButton = new Button("产品介绍");
        productIntroductionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String appSavePath = SysConfigAction.createAppLocalPath();
                AlertUtils.saveProductIntroduction(appSavePath);
                AlertUtils.openExplorer(appSavePath);
            }
        });


        Text adv = new Text("开源，100%离线使用，免费");
        Text version = new Text("water chat version 24.");
        Text allRight = new Text("All Rights Reserved.");
        VBox vBox = new VBox(modelsChoiceBox, logChoiceBox, tipsButton, productIntroductionButton, adv, version, allRight);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(4);

        AnchorPane statementPane = new AnchorPane(vBox);
        AnchorPane.setTopAnchor(vBox, 100.0);
        AnchorPane.setLeftAnchor(vBox, 2.0);
        AnchorPane.setRightAnchor(vBox, 2.0);

        VBox vb = new VBox();
        vb.getChildren().addAll(statementPane);
        vb.setSpacing(2);
        HBox hBox = new HBox(vb);
        return hBox;
    }

    public static String getTipsMsg() {
        return "1. 应用在调用AI大模型期间，应用界面不可操作是正常的，请耐心等候。\n\n"
                + "2. 如果要自行增加Qwen2-0.5B-Instruct以外的AI模型，请查看Help页面下“产品介绍”的文档进行操作。\n\n"
                + "3. 如果询问复杂问题（逻辑推理，代码生成）,那么以CPU运行15亿参数量AI模型时，需要等待（3 ~ 5）分钟也属于正常。\n\n"
                + "4. 在Apple M1 CPU的电脑上（纯CPU运行）询问5亿参数量的AI一般问题时，程序处理时间也需要（5 ~ 30）秒。 较低配置的电脑可能需要等待更长时间。同时复杂问题则需要等待更久。\n\n"
                + "5. 如果电脑有英伟达显卡并且支持cuda加速，那么AI模型会自动使用显卡加速。回答问题的速度会自动加快！\n\n"
                + "6. 如果电脑为Apple M 系列处理器并且MacOS version > 12.3，那么AI模型会自动使用苹果的硬件加速。回答问题的速度会自动加快！\n\n"
                + "7. 运行内存(RAM)参考：5亿参数量的AI模型需要内存 >3GB，15亿参数量的AI模型需要内存 >5GB，70亿参数量的AI模型需要内存 >15GB。\n\n"
                + "8. 本应用占据硬盘空间大是正常的。AI模型参数文件大小参考：5亿参数量～1GB，15亿参数量～4GB，70亿参数量～15GB。\n\n"
                ;
    }


    public static ChoiceBox getModelsChoiceBox() {
        // hardcode
        ArrayList<String> nameChoices = new ArrayList<>();
        for (String modelName : SysConfig.MODEL_NAME_LIST) {
            nameChoices.add("模型：" + modelName);
        }

        ChoiceBox modelsChoiceBox = new ChoiceBox();
        modelsChoiceBox.getItems().addAll(nameChoices);
        modelsChoiceBox.setValue("模型：" + SysConfig.MODEL_NAME);  // （坑）这里 modelsChoiceBox.setValue 的值必须是 nameChoices.add("模型：" + modelName); 里面的值，否则会是空白值
//        modelsChoiceBox.getSelectionModel().select(0);

        modelsChoiceBox.setMinWidth(150);
        modelsChoiceBox.setMaxWidth(800);

        modelsChoiceBox.setOnAction((event) -> {
            int selectedIndex = modelsChoiceBox.getSelectionModel().getSelectedIndex();
            try {
                if (selectedIndex == 0) {
                    SysConfigAction.updateModelName(SysConfig.MODEL_NAME_LIST.get(0));
                    LogUtils.info("选择模型：" + SysConfig.MODEL_NAME_LIST.get(0));
                } else if (selectedIndex == 1) {
                    SysConfigAction.updateModelName(SysConfig.MODEL_NAME_LIST.get(1));
                    LogUtils.info("选择模型：" + SysConfig.MODEL_NAME_LIST.get(1));
                } else if (selectedIndex == 2) {
                    SysConfigAction.updateModelName(SysConfig.MODEL_NAME_LIST.get(2));
                    LogUtils.info("选择模型：" + SysConfig.MODEL_NAME_LIST.get(2));
                } else {
                    SysConfigAction.updateModelName(SysConfig.MODEL_NAME_LIST.get(0));
                    LogUtils.info("选择模型：" + SysConfig.MODEL_NAME_LIST.get(0));
                }
                SysConfigAction.refreshConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return modelsChoiceBox;
    }


    public static ChoiceBox getLogChoiceBox() {
        // hardcode
        ArrayList<String> langChoices = new ArrayList<>();
        langChoices.add("保存会话：Yes");
        langChoices.add("保存会话：No ");

        ChoiceBox sessionLogChoiceBox = new ChoiceBox();
        sessionLogChoiceBox.getItems().addAll(langChoices);
        sessionLogChoiceBox.setValue(SysConfig.IS_LOG_SESSION ? "保存会话：Yes" : "保存会话：No ");

        sessionLogChoiceBox.setMinWidth(100);
        sessionLogChoiceBox.setMaxWidth(120);

        sessionLogChoiceBox.setOnAction((event) -> {
            int selectedIndex = sessionLogChoiceBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) {
                SysConfig.IS_LOG_SESSION = true;
                LogUtils.info("保存会话：Yes");
            } else {
                SysConfig.IS_LOG_SESSION = false;
                LogUtils.info("保存会话：No ");
            }
        });

        return sessionLogChoiceBox;
    }

}
