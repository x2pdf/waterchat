package com.logan.utils;

public class AppSystemOS {
    public static String name =  System.getProperty("os.name");
    public static String arch =  System.getProperty("os.arch");

    public static boolean isMacOS() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return false;
        }
        return true;
    }
}
