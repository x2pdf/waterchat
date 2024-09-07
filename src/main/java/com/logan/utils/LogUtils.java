package com.logan.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logan.chat.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * @author Logan Qin
 * @date 2021/12/27 13:58
 */


public class LogUtils {
    private static ObjectMapper mapper = new ObjectMapper();

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
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
