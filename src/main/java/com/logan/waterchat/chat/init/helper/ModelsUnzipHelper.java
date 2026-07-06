package com.logan.waterchat.chat.init.helper;

import com.logan.waterchat.utils.FileUtils;
import com.logan.waterchat.utils.LogUtils;


public class ModelsUnzipHelper {

    /**
     * 解压模型文件
     * <p>
     * 当检测到行内容包含"models"且文件为.zip格式时，
     * 将模型压缩文件解压到同一目录，并删除原始压缩文件。
     *
     * @param line 需要检查的行内容，用于判断是否包含"models"标识
     * @param configTempPath 配置文件临时路径，与fileName拼接形成完整文件路径
     * @param fileName 文件名，需为.zip格式的压缩文件
     */
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
