package com.logan.waterchat.chat.llamaccp;

import com.logan.waterchat.config.SysConfig;

import java.util.HashMap;

public class LLaMAParams {

    public static String assembleLLaMAParams(HashMap<String, String> allConfigKeyValue) {
        String ngl = "0";
        String temp = "0.6";
        String topK = "20";
        String topP = "0.95";
        String minP = "0.05";
        String presencePenalty = "1.2";
        String c = "2048";
        String n = "2048";
        for (String key : allConfigKeyValue.keySet()) {
            if ("-ngl".equals(key)) {
                ngl = allConfigKeyValue.get(key);
                // 如果启用了vulkan但是配置文件中没有做的对应的设定的情形
                if (SysConfig.IS_USE_VULKAN){
                    if (Integer.parseInt(allConfigKeyValue.get(key)) == 0){
                        ngl = "9999";
                    }
                }
                continue;
            }
            if ("--temp".equals(key)) {
                temp = allConfigKeyValue.get(key);
                continue;
            }
            if ("--top-k".equals(key)) {
                topK = allConfigKeyValue.get(key);
                continue;
            }
            if ("--top-p".equals(key)) {
                topP = allConfigKeyValue.get(key);
                continue;
            }
            if ("--min-p".equals(key)) {
                minP = allConfigKeyValue.get(key);
                continue;
            }
            if ("--presence-penalty".equals(key)) {
                presencePenalty = allConfigKeyValue.get(key);
                continue;
            }
            if ("-c".equals(key)) {
                c = allConfigKeyValue.get(key);
                continue;
            }
            if ("-n".equals(key)) {
                n = allConfigKeyValue.get(key);
                continue;
            }
        }

        String llamaParams = "";
        if (!"*".equals(ngl)) {
            llamaParams = llamaParams + "-ngl " + ngl;
        }
        if (!"*".equals(temp)) {
            llamaParams = llamaParams + " " + "--temp " + temp;
        }
        if (!"*".equals(topK)) {
            llamaParams = llamaParams + " " + "--top-k " + topK;
        }
        if (!"*".equals(topP)) {
            llamaParams = llamaParams + " " + "--top-p " + topP;
        }
        if (!"*".equals(minP)) {
            llamaParams = llamaParams + " " + "--min-p " + minP;
        }
        if (!"*".equals(presencePenalty)) {
            llamaParams = llamaParams + " " + "--presence-penalty " + presencePenalty;
        }
        if (!"*".equals(c)) {
            llamaParams = llamaParams + " " + "-c " + c;
        }
        if (!"*".equals(n)) {
            llamaParams = llamaParams + " " + "-n " + n;
        }

        // --reasoning-budget 0 ，支持 reasoning 的新版 llama.cpp，可以让模型尽量不思考。
        llamaParams = llamaParams + " --reasoning-budget 0 --no-context-shift --ui";
        return llamaParams;
    }

}
