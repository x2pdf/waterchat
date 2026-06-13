package com.logan.waterchat.chat.llamaccp;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logan.waterchat.chat.MessageResDTO;
import com.logan.waterchat.utils.LogUtils;
import java.io.InputStream;
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
    private static String CURRENT_MODEL = LLaMAConf.LLAMA_SERVER_BASE_MODEL;
    private static final String BASE_URL = "http://" + LLaMAConf.LLAMA_SERVER_HOST + ":" + LLaMAConf.LLAMA_SERVER_PORT;
    private static final String CHAT_URL = LLaMAConf.LLAMA_SERVER_BASE_URL;
    private static final String HEALTH_URL = BASE_URL + "/health";
    // 如果服务不需要 API Key 可留空
    private static final String API_KEY = "********";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static int timeout = 600000; // 10 minutes

    public static boolean isLlamaServerHealth() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(3000))
                .build();
        try {
            LogUtils.info("健康检查开始，URL: " + HEALTH_URL);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HEALTH_URL))
                    .timeout(Duration.ofMillis(8000))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            LogUtils.info("健康检查响应码: " + statusCode);

            if (statusCode != 200) {
                LogUtils.error("健康检查失败，响应码: " + statusCode);
                return false;
            }

            String body = response.body();
            LogUtils.info("健康检查响应体: " + body);

            JsonNode node = mapper.readTree(body);
            String status = node.path("status").asText();
            LogUtils.info("健康检查 status 字段: " + status);

            boolean result = "ok".equals(status);
            LogUtils.info("健康检查结果: " + result);
            return result;

        } catch (Throwable e) {
            e.printStackTrace();

            LogUtils.error(
                    "Throwable: " +
                            e.getClass().getName() +
                            " : " +
                            e.getMessage()
            );

            return false;
        }
    }



    public static MessageResDTO sendChat(List<Map<String, String>> messages) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", CURRENT_MODEL);
        body.put("messages", messages);
//        body.put("temperature", 0.7);
        body.put("reasoning_budget", 1024);
        String json = MAPPER.writeValueAsString(body);
        LogUtils.info("req body json: " + json);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(3000))
                .build();

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(CHAT_URL))
                    .timeout(Duration.ofMillis(timeout))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));

            if (API_KEY != null && !API_KEY.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + API_KEY);
            }

            HttpRequest post = requestBuilder.build();
            HttpResponse<InputStream> response = client.send(post, HttpResponse.BodyHandlers.ofInputStream());
            Map<?, ?> result = MAPPER.readValue(response.body(), Map.class);
            // ===== 解析 OpenAI 格式 =====
            List<?> choices = (List<?>) result.get("choices");
            if (choices == null || choices.isEmpty()) {
                return new MessageResDTO();
            }
//            LogUtils.info("respond choices: " + choices);
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
        } catch (Exception e) {
            LogUtils.error("sendChat error: " + e.toString());
            throw e;
        }
    }


}


