package com.logan.waterchat.chat.llamaccp;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Collects the useful acceleration details from llama.cpp's verbose startup output. */
final class LLaMARuntimeDiagnostics {
    private static final Pattern DEVICE_PATTERN = Pattern.compile(
            "(?i)(?:found device|using device)\\s*(?:\\d+)?\\s*:?\\s*(.+)");
    private static final Pattern OFFLOAD_PATTERN = Pattern.compile(
            "(?i)offloaded\\s+(\\d+)(?:/(\\d+))?\\s+layers?\\s+to GPU");

    private boolean metalInitialized;
    private String gpuDevice;
    private String offloadDescription;

    String inspect(String line) {
        String lowerLine = line.toLowerCase(Locale.ROOT);

        if (!metalInitialized && (lowerLine.contains("ggml_metal_init")
                || lowerLine.contains("metal backend"))) {
            metalInitialized = true;
            Matcher deviceMatcher = DEVICE_PATTERN.matcher(line);
            if (deviceMatcher.find()) {
                gpuDevice = deviceMatcher.group(1).trim();
                return "GPU 加速后端已初始化：Metal；设备=" + gpuDevice;
            }
            return "GPU 加速后端已初始化：Metal";
        }

        Matcher deviceMatcher = DEVICE_PATTERN.matcher(line);
        if (gpuDevice == null && lowerLine.contains("metal") && deviceMatcher.find()) {
            gpuDevice = deviceMatcher.group(1).trim();
            return "检测到 Metal GPU 设备：" + gpuDevice;
        }

        Matcher offloadMatcher = OFFLOAD_PATTERN.matcher(line);
        if (offloadMatcher.find()) {
            String loaded = offloadMatcher.group(1);
            String total = offloadMatcher.group(2);
            offloadDescription = total == null ? loaded + " 层" : loaded + "/" + total + " 层";
            return "模型 GPU 卸载完成：" + offloadDescription;
        }

        return null;
    }

    String summary() {
        if (!metalInitialized && offloadDescription == null) {
            return "运行时加速状态：未检测到 Metal/GPU 初始化信息，当前可能使用 CPU";
        }

        StringBuilder summary = new StringBuilder("运行时加速状态：Metal GPU 已启用");
        if (gpuDevice != null) {
            summary.append("；设备=").append(gpuDevice);
        }
        if (offloadDescription != null) {
            summary.append("；GPU 层=").append(offloadDescription);
        }
        return summary.toString();
    }
}
