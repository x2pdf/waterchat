package com.logan.waterchat.chat.longtext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.ToIntFunction;

public class LongTextChunker {
    private final int tokenBudget;
    private final int maxChunks;
    private final ToIntFunction<String> tokenCounter;

    public LongTextChunker(int tokenBudget, int maxChunks, ToIntFunction<String> tokenCounter) {
        this.tokenBudget = tokenBudget;
        this.maxChunks = maxChunks;
        this.tokenCounter = tokenCounter;
    }

    public List<String> split(String text, BooleanSupplier cancelled) throws LongTextTooLargeException {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        if (tokenCounter.applyAsInt(text) <= tokenBudget) {
            chunks.add(text);
            return chunks;
        }

        int targetCharacters = Math.max(1, tokenBudget * 3);
        int start = 0;
        while (start < text.length()) {
            checkCancelled(cancelled);
            int tentativeEnd = Math.min(text.length(), start + targetCharacters);
            int end = findLineBoundary(text, start, tentativeEnd);
            if (end <= start) {
                end = tentativeEnd;
            }
            splitToFit(text.substring(start, end), chunks, cancelled);
            start = end;
        }
        return chunks;
    }

    private void splitToFit(String candidate, List<String> chunks, BooleanSupplier cancelled)
            throws LongTextTooLargeException {
        checkCancelled(cancelled);
        if (tokenCounter.applyAsInt(candidate) <= tokenBudget) {
            addChunk(chunks, candidate);
            return;
        }
        if (candidate.length() <= 1) {
            throw new LongTextTooLargeException("单个字符无法放入模型上下文。");
        }
        int middle = candidate.length() / 2;
        int split = candidate.lastIndexOf('\n', middle);
        if (split <= 0) {
            split = candidate.indexOf('\n', middle);
        }
        if (split <= 0 || split >= candidate.length() - 1) {
            split = middle;
        } else {
            split++;
        }
        splitToFit(candidate.substring(0, split), chunks, cancelled);
        splitToFit(candidate.substring(split), chunks, cancelled);
    }

    private void addChunk(List<String> chunks, String chunk) throws LongTextTooLargeException {
        if (chunks.size() >= maxChunks) {
            throw new LongTextTooLargeException("文本超过 " + maxChunks
                    + " 个分析块，请拆分日志后重试。");
        }
        chunks.add(chunk);
    }

    private static int findLineBoundary(String text, int start, int tentativeEnd) {
        if (tentativeEnd >= text.length()) {
            return text.length();
        }
        int lineEnd = text.lastIndexOf('\n', tentativeEnd);
        return lineEnd >= start ? lineEnd + 1 : tentativeEnd;
    }

    private static void checkCancelled(BooleanSupplier cancelled) {
        if (cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) {
            throw new IllegalStateException("分析已取消");
        }
    }
}
