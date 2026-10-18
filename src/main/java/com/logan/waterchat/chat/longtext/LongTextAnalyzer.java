package com.logan.waterchat.chat.longtext;

import com.logan.waterchat.chat.llamaccp.AppHttpUtils;
import com.logan.waterchat.chat.llamaccp.LLaMAServerCtrl;
import com.logan.waterchat.chat.model.MessageResDTO;
import com.logan.waterchat.config.SysConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class LongTextAnalyzer {
    private static final int CHUNK_SUMMARY_TOKENS = 768;

    private LongTextAnalyzer() {
    }

    public static String analyze(String text, Consumer<String> progress, BooleanSupplier cancelled)
            throws Exception {
        progress.accept("正在计算文本长度并分块……");
        LongTextChunker chunker = new LongTextChunker(SysConfig.LONG_TEXT_CHUNK_TOKENS,
                SysConfig.LONG_TEXT_MAX_CHUNKS, AppHttpUtils::countTokens);
        List<String> chunks = chunker.split(text, cancelled);
        List<String> summaries = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            checkCancelled(cancelled);
            progress.accept("正在分析第 " + (i + 1) + "/" + chunks.size() + " 块……");
            MessageResDTO response = AppHttpUtils.sendChat(chunkMessages(chunks.get(i), i, chunks.size()),
                    CHUNK_SUMMARY_TOKENS);
            summaries.add(LLaMAServerCtrl.assembleModelResMessage(response));
        }

        progress.accept("正在综合全部分析结果……");
        String combined = combineSummaries(summaries);
        while (AppHttpUtils.countTokens(combined) > SysConfig.LONG_TEXT_CHUNK_TOKENS) {
            checkCancelled(cancelled);
            List<String> groups = chunker.split(combined, cancelled);
            List<String> reduced = new ArrayList<>();
            for (int i = 0; i < groups.size(); i++) {
                progress.accept("正在压缩汇总材料 " + (i + 1) + "/" + groups.size() + "……");
                MessageResDTO response = AppHttpUtils.sendChat(reduceMessages(groups.get(i)),
                        CHUNK_SUMMARY_TOKENS);
                reduced.add(LLaMAServerCtrl.assembleModelResMessage(response));
            }
            combined = combineSummaries(reduced);
        }
        MessageResDTO finalResponse = AppHttpUtils.sendChat(finalMessages(combined),
                SysConfig.MAX_GENERATION_TOKENS);
        return LLaMAServerCtrl.assembleModelResMessage(finalResponse);
    }

    private static List<Map<String, String>> chunkMessages(String chunk, int index, int total) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", "你正在分块分析一份长日志。只基于当前片段提取事实，保留时间、线程、组件、错误、序列号和因果线索；不要假设未看到的内容。"));
        messages.add(message("user", "这是日志第 " + (index + 1) + "/" + total
                + " 块。请输出结构化的片段摘要、异常证据和可能原因：\n\n" + chunk));
        return messages;
    }

    private static List<Map<String, String>> reduceMessages(String summaries) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", "合并日志片段摘要，去重但保留所有关键证据、时间和标识。"));
        messages.add(message("user", summaries));
        return messages;
    }

    private static List<Map<String, String>> finalMessages(String summaries) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", SysConfig.MODEL_DEFAULT_SYSTEM_PROMPT
                + "\n你需要根据所有片段摘要完成日志问题分析，明确区分事实、推断和待验证项。"));
        messages.add(message("user", "请综合以下全部片段的分析结果，给出总体时间线、核心异常、最可能根因、证据和排查/修复建议：\n\n"
                + summaries));
        return messages;
    }

    private static String combineSummaries(List<String> summaries) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < summaries.size(); i++) {
            result.append("\n\n===== 片段摘要 ").append(i + 1).append(" =====\n")
                    .append(summaries.get(i));
        }
        return result.toString();
    }

    private static Map<String, String> message(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private static void checkCancelled(BooleanSupplier cancelled) {
        if (cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) {
            throw new IllegalStateException("分析已取消");
        }
    }
}
