package com.logan.waterchat.utils;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {


    /**
     * @param fileName 形如： config/config.properties
     * @param filePath 形如：/Users/megan/Downloads/waterchat/resources/config/config.properties
     * @throws IOException
     */
    public void copyFile(String fileName, String filePath) {
        try {
            // 模块化适配方法
            InputStream input = getClass().getResourceAsStream("/" + fileName);
            if (input == null) {
                LogUtils.error("无法找到资源文件: " + fileName);
                return;
            }
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


    public static void extractTarGz(String folderAbsolutePath, String tarGzFileName) throws Exception {
        File dir = new File(folderAbsolutePath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("无效的文件夹路径: " + folderAbsolutePath);
        }
        
        File tarGzFile = new File(dir, tarGzFileName);
        if (!tarGzFile.exists()) {
            throw new FileNotFoundException("tar.gz 文件不存在: " + tarGzFile.getAbsolutePath());
        }
        
        LogUtils.info("开始解压文件: " + tarGzFile.getAbsolutePath());
        
        ProcessBuilder pb = new ProcessBuilder("tar", "-xzvf", tarGzFileName);
        pb.directory(dir);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                LogUtils.info(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String errorMsg = String.format(
                "tar 解压失败，退出码: %d\n文件路径: %s\n输出信息:\n%s",
                exitCode,
                tarGzFile.getAbsolutePath(),
                output.toString()
            );
            LogUtils.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        LogUtils.info("tar 解压成功: " + tarGzFileName);
    }

    public static void deleteFile(String filePath) throws IOException {
        File file = new File(filePath);

        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Failed to delete file: " + filePath);
            }
        }
    }


    // 传入zip文件的绝对路径，包含zip后缀名。
    public static void unzipToSameDirectory(String zipFilePath) throws IOException {

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

    public static String mkDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dirPath;
    }

    public static String chooseFilePath(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(stage);
        if (file == null) {
            return null;
        }
        return file.getAbsolutePath();
    }

    public static String chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("所有文件 (*.*)", "*.*");
        fileChooser.getExtensionFilters().addAll(allFilter);
        // 弹出文件选择对话框
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return null; // 用户取消选择
        }
        return file.getAbsolutePath();
    }
}
