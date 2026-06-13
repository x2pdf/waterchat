package com.logan.waterchat.chat.refresh;

import com.logan.waterchat.WaterChatApplication;
import com.logan.waterchat.config.SysConfig;

public class RefreshUI {

    /**
     * 刷新应用名称
     */
    public static void updateAppName() {
        if (WaterChatApplication.primaryStage != null) {
            WaterChatApplication.primaryStage.setTitle(SysConfig.APP_NAME + " (" + SysConfig.MODEL_NAME + ")");
        }
    }

}
