package com.logan.config;


import com.logan.chat.LLaMAConf;
import com.logan.chat.LLaMAServerCtrl;
import com.logan.utils.LocalFileUtils;
import com.logan.utils.LogUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
            unzipLLaMAFile();
            LogUtils.info("InitSource end");
        } catch (Exception e) {
            LogUtils.error("initSource exception. info: " + e);
        }
    }

    private void unzipLLaMAFile() {
        String llamaExecPath = LLaMAConf.getLLaMAExecAbsPath();
//        LogUtils.info("unzip(tar.gz):  " + llamaExecPath + ".tar.gz");
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                // macOS的情形： 因为zip压缩不会保留可执行文件的元信息导致可执行文件解压之后文件被破坏，所以只能使用 tar.gz 格式
                // TODO 应用内文件名写死了，待优化。
                extractTarGz(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH, "llama-mac-arm64.tar.gz");
                extractTarGz(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH, "llama-mac-x64.tar.gz");
                deleteFile(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH + "/llama-mac-arm64.tar.gz");
                deleteFile(SysConfig.TEMP_RESOURCES_PATH + SysConfig.MODEL_EXEC_PATH + "/llama-mac-x64.tar.gz");
            } else {
                // windows 使用zip压缩文件
                unzipToSameDirectory(llamaExecPath + ".zip");
            }
        } catch (Exception e) {
            LogUtils.error("unzipLLaMAFile exception. info: " + e);
            e.printStackTrace();
        }

//        grantExecutePermissionInFolder(llamaExecPath);
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
                    LogUtils.info("configTempPath: " + configTempPath);
                    copyFile(filePath + fileName, configTempPath + fileName);

                    if (line.contains("modelsexec")) {
                        // MacOS 要授权, 才能执行命令行
                        if (SysConfigAction.isMacOS()) {
                            LogUtils.info("给复制的 modelsexec 文件授权：可执行 chmod +x ****。");
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
            LogUtils.error("移动文件错误, fileName: " + fileName);
            e.printStackTrace();
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

    private static void grantExecutePermissionInFolder(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory: " + directoryPath);
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                boolean success = file.setExecutable(true, false);
                if (!success) {
                    System.err.println("Failed to set executable: " + file.getAbsolutePath());
                }
            }
        }
    }

    public static void extractTarGz(String folderAbsolutePath, String tarGzFileName) throws Exception {
        File dir = new File(folderAbsolutePath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("无效的文件夹路径: " + folderAbsolutePath);
        }
        ProcessBuilder pb = new ProcessBuilder("tar", "-xzvf", tarGzFileName);
        // 设置命令执行目录
        pb.directory(dir);
        // 合并标准输出和错误输出
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 打印执行输出（可选）
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("tar 解压失败，退出码: " + exitCode);
        }
    }

    // 传入zip文件的绝对路径，包含zip后缀名。
    private static void unzipToSameDirectory(String zipFilePath) throws IOException {

        File zipFile = new File(zipFilePath);

        if (!zipFile.exists() || !zipFile.isFile()) {
//            throw new FileNotFoundException("Zip file not found: " + zipFilePath);
            return;
        }

        File targetDir = zipFile.getParentFile();
        byte[] buffer = new byte[8192];

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                File newFile = new File(targetDir, entry.getName());

                // 防止 Zip Slip 漏洞
                String canonicalTargetDirPath = targetDir.getCanonicalPath();
                String canonicalNewFilePath = newFile.getCanonicalPath();
                if (!canonicalNewFilePath.startsWith(canonicalTargetDirPath + File.separator)) {
                    throw new IOException("Blocked potential Zip Slip attack: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // 确保父目录存在
                    new File(newFile.getParent()).mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(newFile);
                         BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {

                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }

                zis.closeEntry();
            }
        }
    }


    private static void deleteFile(String filePath) throws IOException {
        File file = new File(filePath);

        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Failed to delete file: " + filePath);
            }
        }
    }


    /**
     * 生成复制资源路径的开发方法。
     * <p>
     * 该函数执行以下操作：
     * 1. 定义一个绝对文件路径，指向包含模型文件的目录。
     * 2. 调用getFileNames方法获取该目录下的所有文件名。
     * 3. 如果文件列表不为空，则遍历文件名并打印格式化的输出。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        String absFilePath = "/Users/megan/code/waterchat/src/main/resources/modelsexec/llama-mac-arm64";
        ArrayList<String> fileNames = getFileNames(absFilePath);
        if (!fileNames.isEmpty()) {
            for (String fileName : fileNames) {
                System.out.println("modelsexec/llama-mac-arm64/=" + fileName);
            }
        }
    }

    private static ArrayList<String> getFileNames(String folderPath) {
        ArrayList<String> fileNames = new ArrayList<>();
        File folder = new File(folderPath);
        // 判断是否存在且是目录
        if (!folder.exists() || !folder.isDirectory()) {
            return fileNames; // 返回空列表
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) { // 只要文件，不要子目录
                    fileNames.add(file.getName()); // 仅文件名（含后缀）
                }
            }
        }

        return fileNames;
    }

}
