package com.logan.chatui;

import com.logan.chat.llamaccp.LLaMAServerCtrl;
import com.logan.chat.refresh.RefreshConfig;
import com.logan.chat.refresh.RefreshUI;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;
import com.logan.utils.AlertUtils;
import com.logan.utils.LocalFileUtils;
import com.logan.utils.LogUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class HelpPage {

    public AnchorPane getHelpTab() {

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

    public HBox getBoxHelp() {
        ChoiceBox modelsChoiceBox = getModelsChoiceBox();
        ChoiceBox fontSizeChoiceBox = getFontSizeChoiceBox();
        ChoiceBox aiCreativityChoiceBox = getAICreativityChoiceBox();
        ChoiceBox logChoiceBox = getLogChoiceBox();
        ChoiceBox langChoiceBox = getLangChoiceBox();

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
        Text allRight = new Text("All Rights Reserved.");
        VBox vBox;
        if (SysConfig.IS_MACOS) {
            vBox = new VBox(modelsChoiceBox, logChoiceBox, langChoiceBox, fontSizeChoiceBox, aiCreativityChoiceBox, sessionLogButton, tipsButton, productIntroductionButton, adv, version, allRight);
        } else {
            ChoiceBox vulkanChoiceBox = getVulkanChoiceBox();
            vBox = new VBox(modelsChoiceBox, logChoiceBox, langChoiceBox, fontSizeChoiceBox, aiCreativityChoiceBox, vulkanChoiceBox, sessionLogButton, tipsButton, productIntroductionButton, adv, version, allRight);
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


    public static ChoiceBox getModelsChoiceBox() {
        // hardcode
        ArrayList<String> nameChoices = new ArrayList<>();
        for (String modelName : SysConfig.MODEL_NAME_LIST) {
            nameChoices.add(SysConfigAction.getLang("model") + "：" + modelName);
        }

        ChoiceBox modelsChoiceBox = new ChoiceBox();
        modelsChoiceBox.getItems().addAll(nameChoices);
        modelsChoiceBox.setValue(SysConfigAction.getLang("model") + "：" + SysConfig.MODEL_NAME);  // （坑）这里 modelsChoiceBox.setValue 的值必须是 nameChoices.add("模型：" + modelName); 里面的值，否则会是空白值

        modelsChoiceBox.setMinWidth(150);
        modelsChoiceBox.setMaxWidth(1000);

        modelsChoiceBox.setOnAction((event) -> {
            int selectedIndex = modelsChoiceBox.getSelectionModel().getSelectedIndex();
            try {
                for (int i = 0; i < nameChoices.size(); i++) {
                    if (i != selectedIndex){
                        continue;
                    }else {
                        SysConfig.MODEL_NAME = SysConfig.MODEL_NAME_LIST.get(selectedIndex);
                        RefreshUI.updateAppName();
                        LogUtils.info("选择模型：" + SysConfig.MODEL_NAME_LIST.get(selectedIndex));
                    }
                }
                RefreshConfig.refreshConfig();
                LLaMAServerCtrl.refreshModelMmprojInfo();
                LLaMAServerCtrl.restartLLaMAServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return modelsChoiceBox;
    }


    public static ChoiceBox getLogChoiceBox() {
        // hardcode
        ArrayList<String> langChoices = new ArrayList<>();
        langChoices.add(SysConfigAction.getLang("saveConversation") + "：Yes");
        langChoices.add(SysConfigAction.getLang("saveConversation") + "：No ");

        ChoiceBox sessionLogChoiceBox = new ChoiceBox();
        sessionLogChoiceBox.getItems().addAll(langChoices);
        sessionLogChoiceBox.setValue(SysConfig.IS_LOG_SESSION ? SysConfigAction.getLang("saveConversation") + "：Yes" : SysConfigAction.getLang("saveConversation") + "：No ");

        sessionLogChoiceBox.setMinWidth(140);
        sessionLogChoiceBox.setMaxWidth(180);

        sessionLogChoiceBox.setOnAction((event) -> {
            int selectedIndex = sessionLogChoiceBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) {
                SysConfig.IS_LOG_SESSION = true;
                LogUtils.info(SysConfigAction.getLang("saveConversation") + "：Yes");
            } else {
                SysConfig.IS_LOG_SESSION = false;
                LogUtils.info(SysConfigAction.getLang("saveConversation") + "：No ");
            }
        });

        return sessionLogChoiceBox;
    }


    public static ChoiceBox getVulkanChoiceBox() {
        // hardcode
        ArrayList<String> vulkanChoices = new ArrayList<>();
        vulkanChoices.add("使用Vulkan：Yes");
        vulkanChoices.add("使用Vulkan：No ");

        ChoiceBox vulkanChoiceBox = new ChoiceBox();
        vulkanChoiceBox.getItems().addAll(vulkanChoices);
        vulkanChoiceBox.setValue(SysConfig.IS_USE_VULKAN ? "使用Vulkan：Yes" : "使用Vulkan：No ");

        vulkanChoiceBox.setMinWidth(100);
        vulkanChoiceBox.setMaxWidth(120);

        vulkanChoiceBox.setOnAction((event) -> {
            int selectedIndex = vulkanChoiceBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) {
                SysConfig.IS_USE_VULKAN = true;
                LLaMAServerCtrl.restartLLaMAServer();
                LogUtils.info("使用Vulkan：Yes");
                AlertUtils.msg("注意：如果切换到vulkan模式下使用本应用出现问题（应用不响应、崩溃等等），请手动关闭Vulkan模型，不要使用Vulkan。\n本应用并非在所有的环境下都能够正常调用Vulkan。谢谢。");
            } else {
                SysConfig.IS_USE_VULKAN = false;
                LLaMAServerCtrl.restartLLaMAServer();
                LogUtils.info("使用Vulkan：No ");
            }
        });

        return vulkanChoiceBox;
    }


    public static ChoiceBox getFontSizeChoiceBox() {
        // hardcode
        ArrayList<String> fontSizeChoices = new ArrayList<>();
        fontSizeChoices.add(SysConfigAction.getLang("fontSize") + ": " + "10");
        fontSizeChoices.add(SysConfigAction.getLang("fontSize") + ": " + "12");
        fontSizeChoices.add(SysConfigAction.getLang("fontSize") + ": " + "14");
        fontSizeChoices.add(SysConfigAction.getLang("fontSize") + ": " + "16");
        fontSizeChoices.add(SysConfigAction.getLang("fontSize") + ": " + "18");
        fontSizeChoices.add(SysConfigAction.getLang("fontSize") + ": " + "20");
        fontSizeChoices.add(SysConfigAction.getLang("fontSize") + ": " + "24");
        fontSizeChoices.add(SysConfigAction.getLang("fontSize") + ": " + "30");
        ChoiceBox fontSizeChoiceBox = new ChoiceBox();
        fontSizeChoiceBox.getItems().addAll(fontSizeChoices);
        fontSizeChoiceBox.setValue(SysConfigAction.getLang("fontSize") + ": " + SysConfig.FONT_SIZE);
        fontSizeChoiceBox.setMinWidth(100);
        fontSizeChoiceBox.setMaxWidth(120);

        fontSizeChoiceBox.setOnAction((event) -> {
            int selectedIndex = fontSizeChoiceBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) {
                SysConfig.FONT_SIZE = 10;
            } else if (selectedIndex == 1) {
                SysConfig.FONT_SIZE = 12;
            } else if (selectedIndex == 2) {
                SysConfig.FONT_SIZE = 14;
            } else if (selectedIndex == 3) {
                SysConfig.FONT_SIZE = 16;
            } else if (selectedIndex == 4) {
                SysConfig.FONT_SIZE = 18;
            } else if (selectedIndex == 5) {
                SysConfig.FONT_SIZE = 20;
            } else if (selectedIndex == 6) {
                SysConfig.FONT_SIZE = 24;
            } else if (selectedIndex == 7) {
                SysConfig.FONT_SIZE = 30;
            } else {
                SysConfig.FONT_SIZE = 16;
            }

            SysConfigAction.updateConfigFontSize(SysConfig.FONT_SIZE);
            // 刷新字体大小
            Homepage.freshChatMsgBox();
        });

        return fontSizeChoiceBox;
    }

    public static ChoiceBox getAICreativityChoiceBox() {
        // hardcode
        ArrayList<String> AICreavitityChoices = new ArrayList<>();
        AICreavitityChoices.add(SysConfigAction.getLang("AICreativity") + ": " + "0.1");
        AICreavitityChoices.add(SysConfigAction.getLang("AICreativity") + ": " + "0.2");
        AICreavitityChoices.add(SysConfigAction.getLang("AICreativity") + ": " + "0.3");
        AICreavitityChoices.add(SysConfigAction.getLang("AICreativity") + ": " + "0.4");
        AICreavitityChoices.add(SysConfigAction.getLang("AICreativity") + ": " + "0.5");
        AICreavitityChoices.add(SysConfigAction.getLang("AICreativity") + ": " + "0.6");
        AICreavitityChoices.add(SysConfigAction.getLang("AICreativity") + ": " + "0.7");
        AICreavitityChoices.add(SysConfigAction.getLang("AICreativity") + ": " + "0.8");
        AICreavitityChoices.add(SysConfigAction.getLang("AICreativity") + ": " + "0.9");
        ChoiceBox AICreativityChoiceBox = new ChoiceBox();
        AICreativityChoiceBox.getItems().addAll(AICreavitityChoices);
        AICreativityChoiceBox.setValue(SysConfigAction.getLang("AICreativity") + ": " + SysConfig.AI_CREATIVITY);
        AICreativityChoiceBox.setMinWidth(100);
        AICreativityChoiceBox.setMaxWidth(120);

        AICreativityChoiceBox.setOnAction((event) -> {
            int selectedIndex = AICreativityChoiceBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) {
                SysConfig.AI_CREATIVITY = 0.1;
            } else if (selectedIndex == 1) {
                SysConfig.AI_CREATIVITY = 0.2;
            } else if (selectedIndex == 2) {
                SysConfig.AI_CREATIVITY = 0.3;
            } else if (selectedIndex == 3) {
                SysConfig.AI_CREATIVITY = 0.4;
            } else if (selectedIndex == 4) {
                SysConfig.AI_CREATIVITY = 0.5;
            } else if (selectedIndex == 5) {
                SysConfig.AI_CREATIVITY = 0.6;
            } else if (selectedIndex == 6) {
                SysConfig.AI_CREATIVITY = 0.7;
            } else if (selectedIndex == 7) {
                SysConfig.AI_CREATIVITY = 0.8;
            } else if (selectedIndex == 8) {
                SysConfig.AI_CREATIVITY = 0.9;
            } else {
                SysConfig.AI_CREATIVITY = 0.3;
            }

            SysConfigAction.updateConfigAICreativity(SysConfig.AI_CREATIVITY);
            LLaMAServerCtrl.restartLLaMAServer();
        });

        return AICreativityChoiceBox;
    }


    public ChoiceBox getLangChoiceBox() {
        ArrayList<String> langChoices = new ArrayList<>();
        langChoices.add("语言：中文");
        langChoices.add("language: English");

        ChoiceBox langChoiceBox = new ChoiceBox();
        langChoiceBox.getItems().addAll(langChoices);
        langChoiceBox.setValue(SysConfig.LANG.equals("cn") ? "语言：中文" : "language: English");

        langChoiceBox.setMinWidth(140);
        langChoiceBox.setMaxWidth(180);

        langChoiceBox.setOnAction((event) -> {
            int selectedIndex = langChoiceBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) {
                SysConfig.LANG = "cn";
            } else {
                SysConfig.LANG = "en";
            }

            SysConfigAction.updateConfigLanguage(SysConfig.LANG);
            changeLangFile(SysConfig.LANG);

            Alert warning = new Alert(Alert.AlertType.INFORMATION);
            warning.setTitle("Info");
            warning.setContentText(SysConfigAction.getLang("LanguageSwitch"));
            warning.showAndWait();
        });


        return langChoiceBox;
    }


    public void changeLangFile(String lang) {
        try {
            // 先删除之前的文件
            String langTemp1 = LocalFileUtils.mkTempDir("language");
            LocalFileUtils.deleteDirectory(langTemp1);
            String langTemp = LocalFileUtils.mkTempDir("language");

            if (SysConfig.LANG.equalsIgnoreCase("en")) {
                LocalFileUtils.save2TempDir(LocalFileUtils.is2Byte(Objects.requireNonNull(getClass().getClassLoader()
                                .getResourceAsStream("lang/" + "en.properties"))),
                        langTemp, "en.properties");
            } else if (SysConfig.LANG.equalsIgnoreCase("cn")) {
                LocalFileUtils.save2TempDir(LocalFileUtils.is2Byte(Objects.requireNonNull(getClass().getClassLoader()
                                .getResourceAsStream("lang/" + "cn.properties"))),
                        langTemp, "cn.properties");
            } else {
                LocalFileUtils.save2TempDir(LocalFileUtils.is2Byte(Objects.requireNonNull(getClass().getClassLoader()
                                .getResourceAsStream("lang/" + "en.properties"))),
                        langTemp, "en.properties");
            }

            LogUtils.info("lang change to:" + lang);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
