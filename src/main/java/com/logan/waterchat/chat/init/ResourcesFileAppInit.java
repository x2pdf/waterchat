package com.logan.waterchat.chat.init;


import com.logan.waterchat.chat.init.helper.ModelsExecHelper;
import com.logan.waterchat.chat.init.helper.ModelsUnzipHelper;
import com.logan.waterchat.chat.llamaccp.LLaMAConf;
import com.logan.waterchat.config.SysConfig;
import com.logan.waterchat.config.SysConfigAction;
import com.logan.waterchat.utils.FileUtils;
import com.logan.waterchat.utils.LocalFileUtils;
import com.logan.waterchat.utils.LogUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

public class ResourcesFileAppInit implements AppInitInterface {

    @Override
    public void init() {
        try {
            clear();
            moveFile();
            updateFile();
        } catch (IOException e) {
            LogUtils.error("ResourcesFileAppInit init error: "+ e);
        }
    }

    public void clear() {
        // 清除所有旧文件
        LocalFileUtils.deleteFolder(SysConfig.TEMP_RESOURCES_PATH);
        LocalFileUtils.makeDir(SysConfig.TEMP_RESOURCES_PATH);
    }

    public void moveFile() throws IOException {
        moveReadmeFile();
        moveFileItems();
    }

    public void updateFile(){
        createModelsExecFile();
        unzipLLaMAFile();
    }


    /**
     * hardcode here
     *
     * @throws IOException
     */
    private void moveReadmeFile() {
        String fileName = "请不要随意删除本文件夹下的文件.txt";
        FileUtils fileUtils = new FileUtils();
        fileUtils.copyFile("asset/" + fileName, SysConfig.TEMP_RESOURCES_PATH + fileName);
    }



    /**
     * 将jar包中的 resource/config/resourcefilepath.properties 配置的所有文件，复制到APP本地到文件夹下 resources 文件夹当中
     *
     * @throws IOException
     */
    private void moveFileItems() throws IOException {
        FileUtils.mkDir(SysConfig.APP_DOWNLOAD_PATH + "/resources/modelsexec");
        // 模块化适配方法
        InputStream resourceAsStream = getClass().getResourceAsStream("/" + SysConfig.RESOURCE_MOVE_CONFIG_PATH);
        
        Properties properties = new Properties();
        properties.load(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
        
        for (String key : properties.stringPropertyNames()) {
            String fileAbsName = properties.getProperty(key).trim();
            LogUtils.info("moveFile: " + fileAbsName);
            String directoryPath = fileAbsName.substring(0, fileAbsName.lastIndexOf('/') + 1);
            FileUtils.mkDir(SysConfig.APP_DOWNLOAD_PATH + "resources/" + directoryPath);
            String configTempPathFileName = SysConfig.APP_DOWNLOAD_PATH + "resources/" + fileAbsName;

            FileUtils fileUtils = new FileUtils();
            fileUtils.copyFile(fileAbsName, configTempPathFileName);

            // 特殊处理
            ModelsExecHelper.setChomd2ExecFiles(fileAbsName, configTempPathFileName);
            ModelsUnzipHelper.unzipModelFile(fileAbsName,  configTempPathFileName);
        }
    }


    public static ArrayList<String> readFileLines(InputStream inputStream) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }


    private void unzipLLaMAFile() {
        String llamaExecPath = LLaMAConf.getLLaMAExecAbsPath();
        String tarGzFileName = "llama-mac-arm64.tar.gz";
        String tarGzFilePath = SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH + "/" + tarGzFileName;
        
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                File tarGzFile = new File(tarGzFilePath);
                if (!tarGzFile.exists()) {
                    LogUtils.error("tar.gz 文件不存在: " + tarGzFilePath);
                    return;
                }
                
                LogUtils.info("开始解压 macOS llama 文件: " + tarGzFilePath);
                FileUtils.extractTarGz(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH, tarGzFileName);
                
                if (tarGzFile.exists()) {
                    FileUtils.deleteFile(tarGzFilePath);
                    LogUtils.info("已删除压缩包: " + tarGzFilePath);
                }
                
                grantMacOSPermissions(llamaExecPath);
            } else {
                String zipFilePath = llamaExecPath + ".zip";
                File zipFile = new File(zipFilePath);
                if (!zipFile.exists()) {
                    LogUtils.error("zip 文件不存在: " + zipFilePath);
                    return;
                }
                
                LogUtils.info("开始解压 Windows llama 文件: " + zipFilePath);
                FileUtils.unzipToSameDirectory(zipFilePath);
            }
        } catch (Exception e) {
            LogUtils.error("unzipLLaMAFile exception. info: " + e);
            e.printStackTrace();
        }
    }

    private void grantMacOSPermissions(String targetPath) {
        try {
            LogUtils.info("开始给 macOS 解压的文件授权...");

            ProcessBuilder xattrProcess = new ProcessBuilder(
                    "xattr", "-r", "-d", "com.apple.quarantine", targetPath
            );
            xattrProcess.start().waitFor();
            LogUtils.info("已移除 quarantine 属性: " + targetPath);

            ProcessBuilder chmodProcess = new ProcessBuilder(
                    "chmod", "-R", "+x", targetPath
            );
            chmodProcess.start().waitFor();
            LogUtils.info("已添加执行权限: " + targetPath);

            LogUtils.info("macOS 文件授权完成");
        } catch (Exception e) {
            LogUtils.error("grantMacOSPermissions 失败: " + e.getMessage());
        }
    }

    public void createModelsExecFile(){
        File file = new File(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH);
        if (!file.exists()){
            file.mkdirs();
        }
    }


    public void changeLangFile(String lang) {
        try {
            // 先删除之前的文件
            String langTemp1 = LocalFileUtils.mkTempDir("language");
            LocalFileUtils.deleteDirectory(langTemp1);
            String langTemp = LocalFileUtils.mkTempDir("language");

            if (SysConfig.LANG.equalsIgnoreCase("en")) {
                LocalFileUtils.save2TempDir(LocalFileUtils.is2Byte(Objects.requireNonNull(getClass()
                                .getResourceAsStream("/lang/" + "en.properties"))),
                        langTemp, "en.properties");
            } else if (SysConfig.LANG.equalsIgnoreCase("cn")) {
                LocalFileUtils.save2TempDir(LocalFileUtils.is2Byte(Objects.requireNonNull(getClass()
                                .getResourceAsStream("/lang/" + "cn.properties"))),
                        langTemp, "cn.properties");
            } else {
                LocalFileUtils.save2TempDir(LocalFileUtils.is2Byte(Objects.requireNonNull(getClass()
                                .getResourceAsStream("/lang/" + "en.properties"))),
                        langTemp, "en.properties");
            }

            LogUtils.info("lang change to:" + lang);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
