package com.logan.waterchat.chat.longtext;

public final class LongTextUtils {
    public static final int FULL_DISPLAY_MAX_CHARS = 20_000;
    public static final int FULL_DISPLAY_MAX_LINES = 300;
    public static final int PREVIEW_EDGE_LINES = 100;

    private LongTextUtils() {
    }

    public static int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }

    public static boolean needsPreview(String text) {
        return text != null && (text.length() > FULL_DISPLAY_MAX_CHARS
                || countLinesUpTo(text, FULL_DISPLAY_MAX_LINES + 1) > FULL_DISPLAY_MAX_LINES);
    }

    public static String preparingDisplay(String text) {
        if (!needsPreview(text)) {
            return text;
        }
        return "[长文本，正在准备预览：" + text.length() + " 字符]";
    }

    public static String createPreview(String text) {
        if (!needsPreview(text)) {
            return text;
        }
        String[] lines = text.split("\\R", -1);
        StringBuilder preview = new StringBuilder(FULL_DISPLAY_MAX_CHARS);
        preview.append("[长文本预览：共 ").append(lines.length).append(" 行，")
                .append(text.length()).append(" 字符]\n\n");
        appendLines(preview, lines, 0, Math.min(PREVIEW_EDGE_LINES, lines.length));
        int omitted = Math.max(0, lines.length - PREVIEW_EDGE_LINES * 2);
        if (omitted > 0) {
            preview.append("\n…… 已省略中间 ").append(omitted).append(" 行 ……\n\n");
            appendLines(preview, lines, Math.max(PREVIEW_EDGE_LINES, lines.length - PREVIEW_EDGE_LINES), lines.length);
        }
        if (preview.length() > FULL_DISPLAY_MAX_CHARS) {
            return preview.substring(0, FULL_DISPLAY_MAX_CHARS)
                    + "\n…… 预览已限制为 " + FULL_DISPLAY_MAX_CHARS + " 字符 ……";
        }
        return preview.toString();
    }

    private static int countLinesUpTo(String text, int limit) {
        int lines = text.isEmpty() ? 0 : 1;
        for (int i = 0; i < text.length() && lines < limit; i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }

    private static void appendLines(StringBuilder target, String[] lines, int from, int to) {
        for (int i = from; i < to; i++) {
            target.append(lines[i]);
            if (i + 1 < to) {
                target.append('\n');
            }
            if (target.length() >= FULL_DISPLAY_MAX_CHARS) {
                return;
            }
        }
    }
}
