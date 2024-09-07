package com.logan.config;


import com.logan.utils.LocalFileUtils;
import com.logan.utils.LogUtils;

import java.io.*;
import java.util.*;

/**
 * @author Logan Qin
 */
public class InitSource {

    public void init() {
        try {
            LogUtils.info("InitSource start");
            LogUtils.info("os.name:" + System.getProperty("os.name"));
            LogUtils.info("os.arch:" + System.getProperty("os.arch"));
            // 清除所有旧文件
            LocalFileUtils.deleteFolder(SysConfig.TEMP_RESOURCES_PATH);
            LocalFileUtils.makeDir(SysConfig.TEMP_RESOURCES_PATH);
            moveReadmeFile();
            moveFile();

            LogUtils.info("InitSource end");
        } catch (Exception e) {
            LogUtils.error("initSource exception. info: " + e);
        }
    }


    /**
     * 将jar包中的 resource/config/resourcefilepath.txt 配置的所有文件，复制到APP本地到文件夹下 resources 文件夹当中
     *
     * @throws IOException
     */
    private void moveFile() throws IOException {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(SysConfig.RESOURCE_MOVE_CONFIG_PATH);
        ArrayList<String> lines = readFileLines(resourceAsStream);
        if (lines.size() > 0) {
            for (String line : lines) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String filePath = parts[0].trim();
                    String fileName = parts[1].trim();
                    LogUtils.info("moveFile: " + filePath + fileName);
                    String configTempPath = LocalFileUtils.mkTempResourcesDir("resources/" + filePath);
                    copyFile(filePath + fileName, configTempPath + fileName);

                    if (line.contains("modelsexec")) {
                        LogUtils.info("给复制的 modelsexec 文件授权：可执行 chmod +x ****。");
                        // MacOS 要授权, 才能执行命令行
                        if (SysConfigAction.isMacOS()) {
                            Process process = Runtime.getRuntime().exec("chmod +x " + configTempPath + fileName);
                        }
                    }
                }
            }
        }
    }

    /**
     * hardcode here
     *
     * @throws IOException
     */
    private void moveReadmeFile() throws IOException {
        String fileName = "请不要随意删除本文件夹下的文件.txt";
        copyFile("asset/" + fileName, SysConfig.TEMP_RESOURCES_PATH + fileName);
    }


    /**
     * @param fileName 形如： config/config.txt
     * @param filePath 形如：/Users/megan/Downloads/waterchat/resources/config/config.txt
     * @throws IOException
     */
    public void copyFile(String fileName, String filePath) {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream(fileName);
            OutputStream output = null;
            output = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024 * 1024 * 10]; // 10MB缓冲区
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            input.close();
            output.close();
        } catch (IOException e) {
            LogUtils.error("移动文件错误, fileName: "+ fileName);
            e.printStackTrace();
        }
    }


    public static ArrayList<String> readFileLines(InputStream inputStream) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

}
