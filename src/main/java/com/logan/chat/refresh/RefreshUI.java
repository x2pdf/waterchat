package com.logan.chat.refresh;

import com.logan.App;
import com.logan.config.SysConfig;

public class RefreshUI {

    /**
     * 刷新应用名称
     */
    public static void updateAppName() {
        if (App.primaryStage != null) {
            App.primaryStage.setTitle(SysConfig.APP_NAME + " (" + SysConfig.MODEL_NAME + ")");
        }
    }

}
