package com.logan.waterchat.chat.init;


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
     * 将jar包中的 resource/config/resourcefilepath.txt 配置的所有文件，复制到APP本地到文件夹下 resources 文件夹当中
     *
     * @throws IOException
     */
    private void moveFileItems() throws IOException {
        FileUtils.mkDir(SysConfig.APP_DOWNLOAD_PATH + "/resources/modelsexec");
//        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(SysConfig.RESOURCE_MOVE_CONFIG_PATH);
        // 模块化适配方法
        InputStream resourceAsStream = getClass().getResourceAsStream("/" + SysConfig.RESOURCE_MOVE_CONFIG_PATH);
        ArrayList<String> lines = readFileLines(resourceAsStream);
        if (lines.size() > 0) {
            for (String line : lines) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String filePath = parts[0].trim();
                    String fileName = parts[1].trim();
                    LogUtils.info("moveFile: " + filePath + fileName);
                    String configTempPath = FileUtils.mkDir(SysConfig.APP_DOWNLOAD_PATH + "resources/" + filePath);

                    FileUtils fileUtils = new FileUtils();
                    fileUtils.copyFile(filePath + fileName, configTempPath + fileName);

                    if (line.contains("modelsexec")) {
                        // MacOS 要授权, 才能执行命令行
                        if (SysConfigAction.isMacOS()) {
                            LogUtils.info("给复制的 modelsexec 文件授权：可执行 chmod +x. file: " + configTempPath + fileName);
                            Process process = Runtime.getRuntime().exec("chmod +x " + configTempPath + fileName);
                        }
                    }
                    if (SysConfigAction.isMacOS() && !line.contains("modelsexec")) {
                        try {
                            ProcessBuilder xattrProcess = new ProcessBuilder(
                                    "xattr", "-d", "com.apple.quarantine", configTempPath + fileName
                            );
                            xattrProcess.start().waitFor();
                        } catch (Exception e) {
                            LogUtils.error("移除 quarantine 失败: " + e.getMessage());
                        }
                    }
                }
            }
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
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                // macOS的情形： 因为zip压缩不会保留可执行文件的元信息导致可执行文件解压之后文件被破坏，所以只能使用 tar.gz 格式
                // TODO 应用内文件名写死了，待优化。
                FileUtils.extractTarGz(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH, "llama-mac-arm64.tar.gz");
                FileUtils.extractTarGz(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH, "llama-mac-x64.tar.gz");
                FileUtils.deleteFile(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH + "/llama-mac-arm64.tar.gz");
                FileUtils.deleteFile(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH + "/llama-mac-x64.tar.gz");

                grantMacOSPermissions(llamaExecPath);
            } else {
                // windows 使用zip压缩文件
                FileUtils.unzipToSameDirectory(llamaExecPath + ".zip");
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
