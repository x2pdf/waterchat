package com.logan.waterchat.chat.longtext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongTextUtilsTest {
    @Test
    void shortTextIsDisplayedUnchanged() {
        String text = "line one\nline two";
        assertFalse(LongTextUtils.needsPreview(text));
        assertEquals(text, LongTextUtils.createPreview(text));
    }

    @Test
    void longTextGetsBoundedHeadAndTailPreview() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 1_000; i++) {
            text.append("log-line-").append(i).append('\n');
        }
        String preview = LongTextUtils.createPreview(text.toString());
        assertTrue(preview.contains("log-line-0"));
        assertTrue(preview.contains("log-line-999"));
        assertTrue(preview.contains("已省略中间"));
        assertTrue(preview.length() <= LongTextUtils.FULL_DISPLAY_MAX_CHARS + 100);
    }

    @Test
    void countsLinesWithoutRegularExpressions() {
        assertEquals(0, LongTextUtils.countLines(""));
        assertEquals(3, LongTextUtils.countLines("a\nb\n"));
    }
}
