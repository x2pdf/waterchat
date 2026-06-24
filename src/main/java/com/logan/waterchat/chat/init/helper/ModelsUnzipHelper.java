package com.logan.waterchat.chat.init.helper;

import com.logan.waterchat.utils.FileUtils;
import com.logan.waterchat.utils.LogUtils;


public class ModelsUnzipHelper {

    public static void unzipModelFile(String line, String configTempPath, String fileName){
        // 解压模型文件
        if (line.contains("models") && fileName.endsWith(".zip")) {
            // gemma-3-1b-it-Q4_K_M.gguf.zip
            try {
                FileUtils.unzipToSameDirectory(configTempPath + fileName);
                FileUtils.deleteFile(configTempPath + fileName);
            }catch (Exception e){
                LogUtils.error("解压AI模型文件失败: " + e.getMessage());
            }
        }
    }

}
