package com.logan.waterchat.config;


import java.io.File;

public class AppPathConfig {
    public static String getAppDownloadPath(){
        return System.getProperty("user.home") + File.separator + "waterchat" + File.separator;
    }
}
