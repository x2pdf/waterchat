package com.logan.waterchat.config;




import com.logan.waterchat.chat.init.ConfigFileAppInit;
import com.logan.waterchat.chat.init.ResourcesFileAppInit;
import com.logan.waterchat.utils.AppSystemOS;
import com.logan.waterchat.utils.FileUtils;
import com.logan.waterchat.utils.LogUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * @author Logan Qin
 */
public class InitSourceTemplate {

    public void init() {
        try {
            LogUtils.info("InitSourceTemplate start");
            LogUtils.info("os.name:" + AppSystemOS.name);
            LogUtils.info("os.arch:" + AppSystemOS.name);
            init1();
            init2();
            init3();
            initFinally();
            LogUtils.info("InitSourceTemplate end");
        } catch (Exception e) {
            LogUtils.error("InitSourceTemplate exception. error: " + e);
        }
    }


    public void init1(){
        initAppSettingValue();
    }


    public void init2(){

        ResourcesFileAppInit resourcesFileInit = new ResourcesFileAppInit();
        resourcesFileInit.init();

        ConfigFileAppInit configFileAppInit = new ConfigFileAppInit();
        configFileAppInit.init();

    }


    public void init3(){

    }


    public void initFinally(){

    }


    public static void initAppSettingValue(){
        SysConfig.APP_DOWNLOAD_PATH = getAppDownloadPath();
        FileUtils.mkDir(SysConfig.APP_DOWNLOAD_PATH);

        SysConfig.LOG_CACHE_PATH = SysConfig.APP_DOWNLOAD_PATH + "log" + File.separator;
        FileUtils.mkDir(SysConfig.LOG_CACHE_PATH);

        SysConfig.TEMP_RESOURCES_PATH = SysConfig.APP_DOWNLOAD_PATH + "resources" + File.separator;
        FileUtils.mkDir(SysConfig.TEMP_RESOURCES_PATH);

    }


    public static boolean isNeedInitResources() {
        // TODO hard code
        File file = new File(getAppDownloadPath()  + "resources/modelsexec");
        if (file.exists()) {
            return false;
        }
        return true;
    }


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
