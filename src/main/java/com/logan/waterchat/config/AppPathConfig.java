package com.logan.waterchat.config;

import com.logan.waterchat.utils.AppSystemOS;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class AppPathConfig {

    public static String getAppDownloadPath(){
        String appDownloadPath ="";
        File home = FileSystemView.getFileSystemView().getHomeDirectory();
        if (AppSystemOS.isMacOS()) {
            appDownloadPath = home.getAbsolutePath() + File.separator + "Downloads" + File.separator + "waterchat" + File.separator;
        } else {
            // windows多了\\Desktop，所以需要这样子变化 C:\\Users\\lance\\Desktop ==> C:\\Users\\lance
            String originalPath = home.getAbsolutePath();
            int lastIndexOfSeparator = originalPath.lastIndexOf("\\");
            String newPath = originalPath.substring(0, lastIndexOfSeparator);
            appDownloadPath = newPath + File.separator + "Downloads" + File.separator + "waterchat" + File.separator;
        }
        return appDownloadPath;
    }
}
