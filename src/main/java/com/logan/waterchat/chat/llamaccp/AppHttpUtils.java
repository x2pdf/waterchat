package com.logan.waterchat.chat.llamaccp;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logan.waterchat.chat.model.MessageResDTO;
import com.logan.waterchat.config.SysConfig;
import com.logan.waterchat.utils.LogUtils;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class AppHttpUtils {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = "http://" + LLaMAConf.LLAMA_SERVER_HOST + ":" + LLaMAConf.LLAMA_SERVER_PORT;
    private static final String CHAT_URL = LLaMAConf.LLAMA_SERVER_BASE_URL;
    private static final String HEALTH_URL = BASE_URL + "/health";
    private static final String TOKENIZE_URL = BASE_URL + "/tokenize";
    // 如果服务不需要 API Key 可留空
    private static final String API_KEY = "********";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration HEALTH_TIMEOUT = Duration.ofSeconds(2);
    private static volatile boolean tokenizeFallbackLogged = false;
    private static final AtomicLong REQUEST_SEQUENCE = new AtomicLong();

    public static boolean isLlamaServerHealth() {
        long startNanos = System.nanoTime();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HEALTH_URL))
                    .timeout(HEALTH_TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode != 200) {
                LogUtils.error("健康检查失败：响应码=" + statusCode + "，耗时="
                        + elapsedMillis(startNanos) + "ms");
                return false;
            }

            String body = response.body();
            JsonNode node = mapper.readTree(body);
            String status = node.path("status").asText();
            boolean result = "ok".equals(status);
            LogUtils.info("健康检查结果=" + result + "，耗时=" + elapsedMillis(startNanos) + "ms");
            return result;
        } catch (Throwable e) {
            LogUtils.error("健康检查异常：耗时=" + elapsedMillis(startNanos) + "ms，异常=" + e);
            return false;
        }
    }



    public static MessageResDTO sendChat(List<Map<String, String>> messages) throws Exception {
        return sendChat(messages, SysConfig.MAX_GENERATION_TOKENS);
    }

    public static MessageResDTO sendChat(List<Map<String, String>> messages, int maxTokens) throws Exception {
        long requestId = REQUEST_SEQUENCE.incrementAndGet();
        Map<String, Object> body = new HashMap<>();
        body.put("model", SysConfig.MODEL_NAME);
        body.put("messages", messages);
        body.put("reasoning_budget", 0);
        body.put("stream", false);
        body.put("max_tokens", maxTokens);
        String json = MAPPER.writeValueAsString(body);
        int requestCharacters = messages.stream()
                .mapToInt(message -> message.getOrDefault("content", "").length())
                .sum();
        logRequest("[llama-request:" + requestId + "] 开始请求：model=" + SysConfig.MODEL_NAME
                + "，消息数=" + messages.size() + "，字符数=" + requestCharacters
                + "，max_tokens=" + maxTokens + "，timeout="
                + SysConfig.CHAT_REQUEST_TIMEOUT_SECONDS + "s");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();

        long startNanos = System.nanoTime();
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(CHAT_URL))
                    .timeout(Duration.ofSeconds(SysConfig.CHAT_REQUEST_TIMEOUT_SECONDS))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));

            if (API_KEY != null && !API_KEY.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + API_KEY);
            }

            HttpRequest post = requestBuilder.build();
            HttpResponse<String> response = client.send(post, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
            logRequest("[llama-request:" + requestId + "] 响应完成：status=" + response.statusCode()
                    + "，耗时=" + elapsedMillis + "ms，响应字节数="
                    + response.body().getBytes(StandardCharsets.UTF_8).length);

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("llama-server HTTP " + response.statusCode()
                        + "，响应摘要=" + summarize(response.body(), 500));
            }

            Map<?, ?> result = MAPPER.readValue(response.body(), Map.class);
            MessageResDTO message = getMsg(result);
            logRequest("[llama-request:" + requestId + "] 解析完成：contentChars="
                    + length(message.getContent()) + "，reasoningChars="
                    + length(message.getReasoningContent()));
            return message;
        } catch (Exception e) {
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
            String error = "[llama-request:" + requestId + "] 请求失败：耗时=" + elapsedMillis
                    + "ms，异常=" + e.getClass().getSimpleName() + "，详情=" + e.getMessage();
            LogUtils.error(error);
            LogUtils.log2LocalLogFile(error);
            throw e;
        }
    }

    /**
     * Count tokens with llama-server's tokenizer. If the endpoint is unavailable,
     * use a deliberately conservative UTF-8 byte estimate so chunks remain safe.
     */
    public static int countTokens(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        long startNanos = System.nanoTime();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("content", content);
            body.put("add_special", false);
            String json = MAPPER.writeValueAsString(body);
            HttpClient client = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKENIZE_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("tokenize HTTP " + response.statusCode());
            }
            JsonNode tokens = MAPPER.readTree(response.body()).path("tokens");
            if (!tokens.isArray()) {
                throw new IOException("tokenize response has no tokens array");
            }
            int tokenCount = tokens.size();
            LogUtils.info("llama tokenize 完成：字符数=" + content.length() + "，tokens="
                    + tokenCount + "，耗时=" + elapsedMillis(startNanos) + "ms");
            return tokenCount;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("token counting cancelled", e);
        } catch (Exception e) {
            if (!tokenizeFallbackLogged) {
                tokenizeFallbackLogged = true;
                LogUtils.error("tokenize endpoint unavailable; using UTF-8 fallback：耗时="
                        + elapsedMillis(startNanos) + "ms，异常=" + e);
            }
            return content.getBytes(StandardCharsets.UTF_8).length;
        }
    }

    private static MessageResDTO getMsg(Map<?, ?> result){
        // ===== 解析 OpenAI 格式 =====
        List<?> choices = (List<?>) result.get("choices");
        if (choices == null || choices.isEmpty()) {
            return new MessageResDTO();
        }
        Map<?, ?> choice0 = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) choice0.get("message");
        if (message == null) {
            LogUtils.error("llama-server 响应中的首个 choice 缺少 message 字段");
            return new MessageResDTO();
        }

        String content  = (String) message.get("content");
        if (content == null || content.isEmpty()) {
            content = (String) message.get("reasoning_content");
        }

        MessageResDTO messageResDTO = new MessageResDTO();
        messageResDTO.setContent(content);
        messageResDTO.setReasoningContent((String) message.get("reasoning_content"));
        return messageResDTO;
    }

    private static void logRequest(String message) {
        LogUtils.info(message);
        LogUtils.log2LocalLogFile(message);
    }

    private static int length(String value) {
        return value == null ? 0 : value.length();
    }

    private static String summarize(String value, int maxLength) {
        if (value == null) {
            return "<empty>";
        }
        String singleLine = value.replace('\n', ' ').replace('\r', ' ');
        return singleLine.length() <= maxLength
                ? singleLine : singleLine.substring(0, maxLength) + "...";
    }

    private static long elapsedMillis(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
    }


}
