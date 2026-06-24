package com.logan.waterchat.chatui;


import com.logan.waterchat.config.AppUIConfig;
import com.logan.waterchat.config.SysConfig;
import com.logan.waterchat.config.SysConfigAction;
import com.logan.waterchat.utils.LogUtils;
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

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class HelpPage {

    public AnchorPane getHelpTab() {
        HBox boxHelp = getBoxHelp();
        boxHelp.setAlignment(Pos.TOP_CENTER);

        VBox helpVBox = new VBox();
        helpVBox.getChildren().addAll(boxHelp);
        helpVBox.setSpacing(10);
        helpVBox.setPadding(new Insets(AppUIConfig.MARGIN_DEFAULT, 0, AppUIConfig.MARGIN_DEFAULT, 0));
        helpVBox.setAlignment(Pos.BASELINE_CENTER);
        AnchorPane.setTopAnchor(helpVBox, AppUIConfig.MARGIN_DEFAULT);
        AnchorPane.setLeftAnchor(helpVBox, AppUIConfig.MARGIN_DEFAULT);
        AnchorPane.setRightAnchor(helpVBox, AppUIConfig.MARGIN_DEFAULT);
        AnchorPane.setBottomAnchor(helpVBox, AppUIConfig.MARGIN_DEFAULT);

        AnchorPane helpAnchorPane = new AnchorPane();
        helpAnchorPane.setPrefSize(AppUIConfig.STAGE_WIDTH, AppUIConfig.STAGE_HEIGHT);
        helpAnchorPane.getChildren().add(helpVBox);

        return helpAnchorPane;
    }

    public HBox getBoxHelp() {
        ChoiceBox modelsChoiceBox = HelpPageCheckBox.getModelsChoiceBox();
        ChoiceBox fontSizeChoiceBox = HelpPageCheckBox.getFontSizeChoiceBox();
        ChoiceBox aiCreativityChoiceBox = HelpPageCheckBox.getAICreativityChoiceBox();
        ChoiceBox logChoiceBox = HelpPageCheckBox.getLogChoiceBox();
        ChoiceBox langChoiceBox = HelpPageCheckBox.getLangChoiceBox();

        Button sessionLogButton = new Button(SysConfigAction.getLang("openChatLog"));
        sessionLogButton.setOnAction(e -> {
            File folder = new File(SysConfig.APP_DOWNLOAD_PATH);
            if (folder.exists()) {
                try {
                    Desktop.getDesktop().open(folder);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                LogUtils.info("对话记录文件夹不存在");
            }
        });

        Button addAIModelButton = new Button(SysConfigAction.getLang("addAIModel"));
        addAIModelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AddAIModelWindow.openAddModelWindow();
            }
        });

        Button tipsButton = new Button(SysConfigAction.getLang("tips"));
        tipsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AlertUtils.msg(getTipsMsg());
            }
        });

        Button productIntroductionButton = new Button(SysConfigAction.getLang("productIntroduction"));
        productIntroductionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String appSavePath = SysConfig.APP_DOWNLOAD_PATH;
                AlertUtils.saveProductIntroduction(appSavePath);
                AlertUtils.openExplorer(appSavePath);
            }
        });


        Text adv = new Text(SysConfigAction.getLang("productSlogan"));
        Text version = new Text("WaterChat version 26.");
        Text author = new Text("Author: Logan Cham.");
        Text allRight = new Text("All Rights Reserved.");
        VBox vBox;
        if (SysConfig.IS_MACOS) {
            vBox = new VBox(modelsChoiceBox, addAIModelButton, logChoiceBox, langChoiceBox, fontSizeChoiceBox,
                    aiCreativityChoiceBox, sessionLogButton,
                    tipsButton, productIntroductionButton, adv, version, author, allRight);
        } else {
            ChoiceBox vulkanChoiceBox = HelpPageCheckBox.getVulkanChoiceBox();
            vBox = new VBox(modelsChoiceBox, addAIModelButton, logChoiceBox, langChoiceBox, fontSizeChoiceBox,
                    aiCreativityChoiceBox, vulkanChoiceBox, sessionLogButton,
                    tipsButton, productIntroductionButton, adv, version, author, allRight);
        }

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
        if (SysConfig.LANG.equals("cn")) {
            return "1. 应用在调用AI大模型期间，应用界面不可操作是正常的，请耐心等候。\n\n"
                    + "2. 如果要自行增加 Qwen3-0.6B-GGUF（默认模型）以外的AI模型，请查看Help页面下“产品介绍”的文档进行操作。\n\n"
                    + "3. 如果询问复杂问题（逻辑推理，代码生成）,那么以CPU运行15亿参数量AI模型时，需要等待（3 ~ 5）分钟也属于正常。\n\n"
                    + "4. 在Apple M1 CPU的电脑上（纯CPU运行）询问6亿参数量的AI一般问题时，程序处理时间也需要（5 ~ 30）秒。 较低配置的电脑可能需要等待更长时间。同时复杂问题则需要等待更久。\n\n"
                    + "5. 运行内存(RAM)参考：5亿参数量的AI模型需要内存 >3GB，15亿参数量的AI模型需要内存 >5GB，70亿参数量的AI模型需要内存 >15GB。\n\n"
                    + "6. 本应用占据硬盘空间大是正常的。AI模型参数文件大小参考：5亿参数量～1GB，15亿参数量～4GB，70亿参数量～15GB。\n\n"
                    ;
        } else {
            return "1. It is normal for the application interface to be unresponsive while calling a large AI model; please wait patiently.\n" +
                    "\n" +
                    "2. If you want to add an AI model other than Qwen3-0.6B-GGUF (the default model), " +
                    "please refer to the \"Product Introduction\" document on the Help page for instructions.\n" +
                    "\n" +
                    "3. If you are asking complex questions (logical reasoning, code generation), " +
                    "it is normal to wait (3-5) minutes when running a 1.5 billion parameter AI model on the CPU.\n" +
                    "\n" +
                    "4. On a computer with an Apple M1 CPU (running purely on the CPU), " +
                    "when asking a general AI question with 600 million parameters, " +
                    "the program processing time will also take (5-30) seconds. " +
                    "Lower-spec computers may require longer wait times. Complex questions will take even longer.\n" +
                    "\n" +
                    "5. RAM requirements: A 500 million parameter AI model requires >3GB of RAM, " +
                    "a 1.5 billion parameter AI model requires >5GB of RAM, and a 7 billion parameter " +
                    "AI model requires >15GB of RAM. 6. It's normal for this application to occupy a large amount of hard drive space. " +
                    "AI model parameter file size reference: 500 million parameters ~ 1GB, 1.5 billion parameters ~ 4GB, 7 billion parameters ~ 15GB."
                    ;
        }
    }

}
