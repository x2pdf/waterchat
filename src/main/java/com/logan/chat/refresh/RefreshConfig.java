package com.logan.chat.refresh;

import com.logan.chat.init.ConfigFileAppInit;
import com.logan.config.InitSourceTemplate;
import com.logan.config.SysConfig;
import com.logan.utils.LogUtils;

import java.io.IOException;

public class RefreshConfig {

    public static void refreshConfig() throws IOException {
        InitSourceTemplate.initAppSettingValue();
        ConfigFileAppInit.initSystemConfigValue(SysConfig.TEMP_RESOURCES_PATH + SysConfig.CONFIG_PATH);
        ConfigFileAppInit.initLang();
        RefreshUI.updateAppName();
        LogUtils.info("refreshConfig() 配置已更新！");
    }

}
