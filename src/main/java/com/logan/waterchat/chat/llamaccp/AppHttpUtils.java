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

    public static boolean isLlamaServerHealth() {
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
                LogUtils.error("健康检查失败，响应码: " + statusCode);
                return false;
            }

            String body = response.body();
            JsonNode node = mapper.readTree(body);
            String status = node.path("status").asText();
            boolean result = "ok".equals(status);
            LogUtils.info("健康检查结果: " + result);
            return result;
        } catch (Throwable e) {
            LogUtils.error("健康检查异常: " + e);
            return false;
        }
    }



    public static MessageResDTO sendChat(List<Map<String, String>> messages) throws Exception {
        return sendChat(messages, SysConfig.MAX_GENERATION_TOKENS);
    }

    public static MessageResDTO sendChat(List<Map<String, String>> messages, int maxTokens) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", SysConfig.MODEL_NAME);
        body.put("messages", messages);
        body.put("reasoning_budget", 0);
        body.put("stream", false);
        body.put("max_tokens", maxTokens);
        String json = MAPPER.writeValueAsString(body);
        LogUtils.info("开始请求 llama-server，消息数: " + messages.size());

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
            LogUtils.info("llama-server 响应完成，状态码: " + response.statusCode()
                    + "，耗时: " + elapsedMillis + "ms");

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("llama-server HTTP " + response.statusCode() + ": " + response.body());
            }

            Map<?, ?> result = MAPPER.readValue(response.body(), Map.class);
            return getMsg(result);
        } catch (Exception e) {
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
            LogUtils.error("sendChat error after " + elapsedMillis + "ms: " + e);
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
            return tokens.size();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("token counting cancelled", e);
        } catch (Exception e) {
            if (!tokenizeFallbackLogged) {
                tokenizeFallbackLogged = true;
                LogUtils.error("tokenize endpoint unavailable; using UTF-8 fallback: " + e);
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

        String content  = (String) message.get("content");
        if (content == null || content.isEmpty()) {
            content = (String) message.get("reasoning_content");
        }

        MessageResDTO messageResDTO = new MessageResDTO();
        messageResDTO.setContent(content);
        messageResDTO.setReasoningContent((String) message.get("reasoning_content"));
        return messageResDTO;
    }


}
