package com.logan.waterchat.chat.longtext;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LongTextChunkerTest {
    @Test
    void splitsAtLineBoundariesWithinBudget() throws Exception {
        String text = "one\ntwo\nthree\nfour\nfive\n";
        LongTextChunker chunker = new LongTextChunker(10, 20, String::length);
        List<String> chunks = chunker.split(text, () -> false);
        assertEquals(text, String.join("", chunks));
        assertTrue(chunks.size() > 1);
        assertTrue(chunks.stream().allMatch(chunk -> chunk.length() <= 10));
    }

    @Test
    void splitsAnOversizedSingleLine() throws Exception {
        String text = "x".repeat(101);
        LongTextChunker chunker = new LongTextChunker(10, 20, String::length);
        List<String> chunks = chunker.split(text, () -> false);
        assertEquals(text, String.join("", chunks));
        assertTrue(chunks.stream().allMatch(chunk -> chunk.length() <= 10));
    }

    @Test
    void rejectsMoreThanConfiguredChunkCount() {
        String text = "x".repeat(500);
        LongTextChunker chunker = new LongTextChunker(10, 3, String::length);
        assertThrows(LongTextTooLargeException.class, () -> chunker.split(text, () -> false));
    }

    @Test
    void observesCancellation() {
        LongTextChunker chunker = new LongTextChunker(10, 20, String::length);
        assertThrows(IllegalStateException.class,
                () -> chunker.split("x".repeat(100), () -> true));
    }

    @Test
    void handlesARepresentativeTenThousandLineLogWithinTwentyChunks() throws Exception {
        StringBuilder log = new StringBuilder();
        for (int i = 0; i < 10_000; i++) {
            log.append("2026-10-18 INFO line-").append(i).append('\n');
        }
        LongTextChunker chunker = new LongTextChunker(24_000, 20, String::length);
        List<String> chunks = chunker.split(log.toString(), () -> false);
        assertEquals(log.toString(), String.join("", chunks));
        assertTrue(chunks.size() <= 20);
        assertTrue(chunks.stream().allMatch(chunk -> chunk.length() <= 24_000));
    }
}
