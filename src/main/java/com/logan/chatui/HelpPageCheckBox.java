package com.logan.chatui;

import com.logan.chat.init.ResourcesFileAppInit;
import com.logan.chat.llamaccp.LLaMAServerCtrl;
import com.logan.chat.refresh.RefreshConfig;
import com.logan.chat.refresh.RefreshUI;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;
import com.logan.utils.AlertUtils;
import com.logan.utils.LogUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;

import java.io.IOException;
import java.util.ArrayList;

public class HelpPageCheckBox {

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
                RefreshConfig.uiChangeAIModel();
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


    public static ChoiceBox getLangChoiceBox() {
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
            ResourcesFileAppInit resourcesFileAppInit = new ResourcesFileAppInit();
            resourcesFileAppInit.changeLangFile(SysConfig.LANG);

            Alert warning = new Alert(Alert.AlertType.INFORMATION);
            warning.setTitle("Info");
            warning.setContentText(SysConfigAction.getLang("LanguageSwitch"));
            warning.showAndWait();
        });
        return langChoiceBox;
    }

}
