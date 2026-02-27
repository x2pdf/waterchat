package com.logan.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logan.chat.Message;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * @author Logan Qin
 * @date 2021/12/27 13:58
 */


public class LogUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void info(String content) {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        String fmtTime = dtf.format(time);
        System.out.println(fmtTime + " ==== [INFO] " + content);
    }

    public static void error(String content) {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        String fmtTime = dtf.format(time);
        System.err.println(fmtTime + " ==== [ERROR] " + content);

    }

    public static String writeArrayListAsString(ArrayList<Message> list) {
        try {
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将字符串记录到本地的日志文件当中
     * 用于记录应用级别的日志信息。
     *
     * @param msg
     */
    public static void log2LocalLogFile(String msg) {
        try {
            String stringBuilder = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()) +
                    " " + msg + "\n\n";
            LocalFileUtils.appendToMessageFile(stringBuilder,
                    SysConfigAction.createAppLocalPath() + SysConfig.APP_LOG_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 如果文件超过 200MB，则删除前半部分数据，保留后半部分
     */
    public static void trimLogFileIfTooLarge(){
        try {
            trimLogFileIfTooLarge(SysConfigAction.createAppLocalPath() + SysConfig.APP_LOG_FILE_NAME, 200L * 1024 * 1024);
            LogUtils.info("trimLogFileIfTooLarge successful.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果文件超过 maxSizeBytes，则删除前半部分数据，保留后半部分
     */
    public static void trimLogFileIfTooLarge(String filePath, long maxSizeBytes) throws IOException {
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            return;
        }
        long fileSize = file.length();
        if (fileSize <= maxSizeBytes) {
            return; // 不需要处理
        }

        long keepFrom = fileSize / 2; // 保留后半部分
        File tempFile = new File(filePath + ".tmp");
        try (
                RandomAccessFile source = new RandomAccessFile(file, "r");
                RandomAccessFile target = new RandomAccessFile(tempFile, "rw");
                FileChannel sourceChannel = source.getChannel();
                FileChannel targetChannel = target.getChannel()
        ) {
            long bytesToCopy = fileSize - keepFrom;
            sourceChannel.transferTo(keepFrom, bytesToCopy, targetChannel);
        }

        // 原子替换原文件
        Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

}
