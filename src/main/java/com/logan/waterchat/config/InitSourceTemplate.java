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
            // 初始化应用的基础配置，例如文件保存路径，资源路径
            init1();
            // 初始化应用配置文件、执行文件
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
        SysConfig.APP_DOWNLOAD_PATH = AppPathConfig.getAppDownloadPath();
        FileUtils.mkDir(SysConfig.APP_DOWNLOAD_PATH);

        SysConfig.LOG_CACHE_PATH = SysConfig.APP_DOWNLOAD_PATH + "log" + File.separator;
        FileUtils.mkDir(SysConfig.LOG_CACHE_PATH);

        SysConfig.TEMP_RESOURCES_PATH = SysConfig.APP_DOWNLOAD_PATH + "resources" + File.separator;
        FileUtils.mkDir(SysConfig.TEMP_RESOURCES_PATH);

    }


    public static boolean isNeedInitResources() {
        // TODO hard code
        File file = new File(AppPathConfig.getAppDownloadPath()  + "resources/modelsexec");
        if (file.exists()) {
            return false;
        }
        return true;
    }

}
