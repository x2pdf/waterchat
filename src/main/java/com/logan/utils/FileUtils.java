package com.logan.utils;

import com.logan.config.SysConfig;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {


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


    public static void extractTarGz(String folderAbsolutePath, String tarGzFileName) throws Exception {
        File dir = new File(folderAbsolutePath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("无效的文件夹路径: " + folderAbsolutePath);
        }
        System.out.println("folderAbsolutePath：  " + new File(folderAbsolutePath).getAbsolutePath());
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

}
