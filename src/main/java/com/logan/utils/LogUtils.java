package com.logan.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logan.chat.Message;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;

import java.io.IOException;
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
                    " " + msg;
            LocalFileUtils.appendToMessageFile(stringBuilder,
                    SysConfigAction.createAppLocalPath() + SysConfig.APP_LOG_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
