package com.logan.chat.llamaccp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logan.chat.MessageResDTO;
import com.logan.utils.LogUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(3000)
                .setConnectionRequestTimeout(3000)
                .setSocketTimeout(8000)
                .build();
        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build()) {
            HttpGet request = new HttpGet(HEALTH_URL);
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    return false;
                }
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return false;
                }
                // res的格式：
                // {
                //  "status": "ok"
                // }
                String body = EntityUtils.toString(entity, "UTF-8");
                JsonNode node = mapper.readTree(body);
                return "ok".equals(node.path("status").asText());
            }
        } catch (Exception e) {
            return false;
        }
    }


    public static MessageResDTO sendChat(List<Map<String, String>> messages) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", CURRENT_MODEL);
        body.put("messages", messages);
        body.put("temperature", 0.7);
        String json = MAPPER.writeValueAsString(body);
        LogUtils.info("req body json: " + json);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(30000)              // 连接服务器超时
                .setConnectionRequestTimeout(30000)    // 从连接池获取连接超时
                .setSocketTimeout(timeout)               // 等待服务器数据超时
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {
            HttpPost post = new HttpPost(CHAT_URL);
            post.setHeader("Content-Type", "application/json");

            if (API_KEY != null && !API_KEY.isEmpty()) {
                post.setHeader("Authorization", "Bearer " + API_KEY);
            }

            post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
            try (CloseableHttpResponse response = client.execute(post)) {
                InputStream is = response.getEntity().getContent();
                Map<?, ?> result = MAPPER.readValue(is, Map.class);
                // ===== 解析 OpenAI 格式 =====
                List<?> choices = (List<?>) result.get("choices");
                if (choices == null || choices.isEmpty()) {
                    return new MessageResDTO();
                }
                System.out.println("respond choices: " + choices);
                Map<?, ?> choice0 = (Map<?, ?>) choices.get(0);
                Map<?, ?> message = (Map<?, ?>) choice0.get("message");

                MessageResDTO messageResDTO = new MessageResDTO();
                messageResDTO.setContent((String) message.get("content"));
                messageResDTO.setReasoningContent((String) message.get("reasoning_content"));
                return messageResDTO;
            } catch (Exception e) {
                LogUtils.error("sendChat error: " + e.toString());
            }
        } catch (Exception e) {
            LogUtils.error("sendChat error2: " + e.toString());
        }
        return new MessageResDTO();
    }


}
