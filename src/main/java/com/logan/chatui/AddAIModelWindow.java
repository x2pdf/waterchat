package com.logan.chatui;


import com.logan.chat.AIModelNameDTO;
import com.logan.chat.refresh.RefreshConfig;
import com.logan.chat.widgets.AIModelMainFilePathHBox;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;
import com.logan.utils.AlertUtils;
import com.logan.utils.LogUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AddAIModelWindow {
    public static String aiModelFilesPath = "";
    public static String aiModelMainFile = "";

    public static void openAddModelWindow() {
        // 新建一个 Stage，也就是新的窗口
        Stage popupStage = new Stage();
        popupStage.setTitle(SysConfigAction.getLang("addAIModel"));

        AIModelMainFilePathHBox aiModelMainFilePathHBox = new AIModelMainFilePathHBox();
        aiModelMainFilePathHBox.initPane();
        aiModelMainFilePathHBox.setAction(popupStage);


        Button addAIModelConfirmButton = new Button(SysConfigAction.getLang("addAIModelConfirmButton"));
        addAIModelConfirmButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (aiModelMainFile.equals("")){
                    LogUtils.info("aiModelMainFile is empty.");
                    return;
                }
                AIModelNameDTO aiModelNameDTO = copyAIModelFiles(aiModelMainFile, SysConfig.TEMP_RESOURCES_PATH + "models");
                String name = aiModelNameDTO.getName();
                if (name != null && !name.isEmpty()) {
                    SysConfigAction.appendConfigModelNameList(name);
                }
                String nameMmproj = aiModelNameDTO.getNameMmproj();
                if (nameMmproj != null && !nameMmproj.isEmpty()) {
                    SysConfigAction.addConfigModelNameMMProj(name, nameMmproj);
                }

                try {
                    RefreshConfig.refreshConfig();
                } catch (IOException e) {
                   LogUtils.error("addAIModelConfirmButton refreshConfig error: " + e);
                }
                popupStage.close();
                AlertUtils.msg("操作成功！请重启应用。\n\nOperation successful! \n Please restart the application.");
            }
        });
        HBox buttonWrapper = new HBox(addAIModelConfirmButton);
        buttonWrapper.setAlignment(Pos.CENTER);

        VBox addModelVBox = new VBox();
        addModelVBox.setSpacing(5);

        // 给按钮与上方组件增加间距
        addModelVBox.getChildren().add(aiModelMainFilePathHBox.getAnchorPane());
        VBox.setMargin(buttonWrapper, new Insets(50, 0, 0, 0)); // 上方间距20px
        addModelVBox.getChildren().add(buttonWrapper);

        AnchorPane addAIModelAnchorPane = new AnchorPane();
        addAIModelAnchorPane.setPrefSize(600, 400);
        addAIModelAnchorPane.getChildren().add(addModelVBox);
        AnchorPane.setTopAnchor(addModelVBox, 50.0);
        AnchorPane.setLeftAnchor(addModelVBox, 2.0);
        AnchorPane.setRightAnchor(addModelVBox, 2.0);
        AnchorPane.setBottomAnchor(addModelVBox, 2.0);

        Scene popupScene = new Scene(addAIModelAnchorPane, 600, 400);
        popupStage.setScene(popupScene);
        // 显示窗口
        popupStage.show();
    }


    public static AIModelNameDTO copyAIModelFiles(String sourceFilePath, String targetFolderPath) {
        // 获取源文件名和所在文件夹名
        File sourceFile = new File(sourceFilePath);
        String fileName = sourceFile.getName(); // Qwen3.5-0.8B-Q8_0.gguf
        String folderName = sourceFile.getParentFile().getName(); // Qwen3.5-0.8B-GGUF

        // 生成目标文件夹路径
        File targetFolder = new File(targetFolderPath, folderName);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        // 查找源文件夹下是否有包含 "mmproj" 的文件
        String nameMmproj = "";
        File[] filesInSource = sourceFile.getParentFile().listFiles();
        if (filesInSource != null) {
            for (File f : filesInSource) {
                if (f.isFile() && f.getName().contains("mmproj")) {
                    nameMmproj = folderName + "/" + f.getName();
                    break; // 假设最多只有一个
                }
            }
        }

        // 复制所有文件到目标文件夹
        if (filesInSource != null) {
            for (File f : filesInSource) {
                if (f.isFile()) {
                    Path sourcePath = f.toPath();
                    Path targetPath = new File(targetFolder, f.getName()).toPath();
                    try {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LogUtils.error("copyAIModelFiles error, file info: " + f.getName());
                        LogUtils.error("copyAIModelFiles error: " + e);
                    }
                }
            }
        }

        String name = folderName + "/" + fileName;
        // 如果没有找到 mmproj 文件，则设置为空字符串
        if (nameMmproj.isEmpty()) {
            nameMmproj = "";
        }

        AIModelNameDTO aiModelNameDTO = new AIModelNameDTO();
        aiModelNameDTO.setName(name);
        aiModelNameDTO.setNameMmproj(nameMmproj);
        return aiModelNameDTO;
    }


    public static String getAiModelFilesPath() {
        return aiModelFilesPath;
    }

    public static void setAiModelFilesPath(String aiModelFilesPath) {
        AddAIModelWindow.aiModelFilesPath = aiModelFilesPath;
    }

    public static String getAiModelMainFile() {
        return aiModelMainFile;
    }

    public static void setAiModelMainFile(String aiModelMainFile) {
        AddAIModelWindow.aiModelMainFile = aiModelMainFile;
    }
}
