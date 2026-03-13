package com.logan.utils;

import com.logan.config.SysConfig;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ConfigUtils {

    public static HashMap<String, String> parseKeyValueFile(String filePath) throws IOException {
        HashMap<String, String> resultMap = new HashMap<>();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    resultMap.put(parts[0].trim(), parts[1].trim());
//                    System.out.println(" line: " + line);
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        }

        SysConfig.configHashMap = resultMap;
        return resultMap;
    }

}
