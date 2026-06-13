package com.logan.waterchat.chat.refresh;

import com.logan.waterchat.chat.init.ConfigFileAppInit;
import com.logan.waterchat.config.InitSourceTemplate;
import com.logan.waterchat.config.SysConfig;
import com.logan.waterchat.config.SysConfigAction;
import com.logan.waterchat.utils.LogUtils;

import java.io.IOException;

public class RefreshConfig {

    public static void refreshConfig() throws IOException {
        InitSourceTemplate.initAppSettingValue();
        ConfigFileAppInit.initSystemConfigValue(SysConfig.TEMP_RESOURCES_PATH + SysConfig.CONFIG_PATH);
        ConfigFileAppInit.initLang();
        RefreshUI.updateAppName();
        LogUtils.info("refreshConfig() 配置已更新！");
    }


    public static void uiChangeAIModel() {
        SysConfigAction.updateConfigModelName(SysConfig.MODEL_NAME);
        SysConfigAction.updateConfigModelPath(SysConfig.MODEL_NAME);
    }

}
