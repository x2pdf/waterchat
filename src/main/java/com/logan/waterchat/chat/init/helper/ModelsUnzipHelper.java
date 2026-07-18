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
     * @param fileAbsName 配置文件绝对路径文件名
     */
    public static void unzipModelFile(String line,  String fileAbsName){
        // 解压模型文件
        if (line.contains("models") && line.endsWith(".zip")) {
            // gemma-3-1b-it-Q4_K_M.gguf.zip
            try {
                FileUtils.unzipToSameDirectory(fileAbsName);
                FileUtils.deleteFile(fileAbsName);
            }catch (Exception e){
                LogUtils.error("解压AI模型文件失败: " + e.getMessage());
            }
        }
    }

}
