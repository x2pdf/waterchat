package com.logan.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logan.chatui.HomepageAdaptor;
import com.logan.config.SysConfig;
import com.logan.config.SysConfigAction;
import com.logan.utils.AlertUtils;
import com.logan.utils.LogUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LLaMAServerCtrl {
    public static Process process;
    private static final String BASE_URL = LLaMAConf.LLAMA_SERVER_BASE_URL;
    private static String CURRENT_MODEL = LLaMAConf.LLAMA_SERVER_BASE_MODEL;
    // 如果服务不需要 API Key 可留空
    private static final String API_KEY = "********";
    private static final ObjectMapper MAPPER = new ObjectMapper();


    private static void refreshCurrentModel(){
        CURRENT_MODEL = SysConfig.MODEL_NAME;
    }

    public static ModelResMessage sendChat(List<Map<String, String>> messages) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", CURRENT_MODEL);
        body.put("messages", messages);
        body.put("temperature", 0.7);

        String json = MAPPER.writeValueAsString(body);

        LogUtils.info("req body json: " + json);

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(BASE_URL);
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
                    return new ModelResMessage();
                }

                System.out.println("respond choices: " + choices);
                Map<?, ?> choice0 = (Map<?, ?>) choices.get(0);
                Map<?, ?> message = (Map<?, ?>) choice0.get("message");

                ModelResMessage modelResMessage = new ModelResMessage();
                modelResMessage.setContent((String) message.get("content"));
                modelResMessage.setReasoningContent((String) message.get("reasoning_content"));
                return modelResMessage;
            } catch (Exception e) {
                LogUtils.error("sendChat error: " + e.toString());
            }
        } catch (Exception e) {
            LogUtils.error("sendChat error2: " + e.toString());
        }
        return new ModelResMessage();
    }


    public static void startLLaMAServer() {
        try {
//            String command = "/Users/megan/Downloads/llama-b8149/llama-server --host localhost --port 8080 -m /Users/megan/Downloads/waterchat/resources/models/Qwen3-0.6B-GGUF/Qwen3-0.6B-Q8_0.gguf" +
//                    " -ngl 0 --temp 0.6 --top-k 20 --top-p 0.95 --min-p 0.05 --presence-penalty 1.2 -c 16384 -n 4096 --jinja --no-context-shift";

            refreshCurrentModel();

            HashMap<String, String> allConfigKeyValue = SysConfigAction.getAllConfigKeyValue();
            String llamaExecPath = LLaMAConf.getLLaMAExecAbsPath();
            // 使用自定义的llama路径，用户自行设定 cuda llama.ccp的路径
            if (!allConfigKeyValue.isEmpty() && !allConfigKeyValue.get("llama_custom_path").isEmpty()
                    && !allConfigKeyValue.get("llama_custom_path").equals("*")) {
                llamaExecPath = allConfigKeyValue.get("llama_custom_path");
            }

            String command2 = llamaExecPath + "/llama-server" + " --host "
                    + LLaMAConf.LLAMA_SERVER_HOST
                    + " --port " + LLaMAConf.LLAMA_SERVER_PORT
                    + " -m " + SysConfig.TEMP_RESOURCES_PATH + "models/" + CURRENT_MODEL;
            String lLaMAParams = assembleLLaMAParams(allConfigKeyValue);
            if (!lLaMAParams.isEmpty()) {
                command2 = command2 + " " + lLaMAParams;
            }

            LogUtils.info("llama.ccp command: " + command2);
            LogUtils.log2LocalLogFile("llama.ccp command: " + command2);
            process = Runtime.getRuntime().exec(command2);

            // 必须在新线程中消耗输出流，否则几乎必死锁
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LogUtils.info("llama stdout: " + line);
                        LogUtils.log2LocalLogFile("llama stdout: " + line);
                    }
                } catch (Exception e) {
                    LogUtils.error("read stdout error: " + e);
                    LogUtils.log2LocalLogFile("read stdout error: " + e);
                }
            }).start();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LogUtils.info("llama output msg: " + line);
                        if (line.contains("server is listening on")
                                || line.contains("starting the main loop")
                                || line.contains(LLaMAConf.LLAMA_SERVER_HOST + ":" + LLaMAConf.LLAMA_SERVER_PORT)) {
                            LogUtils.info("IS_LLAMA_SERVER_STARTED: true");
                            LLaMAConf.IS_LLAMA_SERVER_STARTED = true;
                        }
                        LogUtils.log2LocalLogFile("llama output msg: " + line);
                    }
                } catch (Exception e) {
                    LogUtils.error("read stderr error: " + e.toString());
                    LogUtils.log2LocalLogFile("read stderr error: " + e.toString());
                }
            }).start();
        } catch (Exception e) {
            LogUtils.info("startLLaMAServer Exception: " + e);
            e.printStackTrace();

            String errorStr = e.toString();
            if (errorStr.contains("llama-win-vulkan-x64/llama-server") && SysConfig.IS_USE_VULKAN){
                AlertUtils.msg("您电脑的环境目前不支持在 Vulkan 模式下运行。目前应用处于异常运行状态。 请关闭Vulkan 模式，回到默认设定。 谢谢！");
            }

        }
    }

    public static void restartLLaMAServer() {
        LogUtils.info("restartLLaMAServer");
        LLaMAServerCtrl.shutdownLLaMAServer();
        LLaMAServerCtrl.startLLaMAServer();
    }

    public static void shutdownLLaMAServer() {
        if (process != null) {
            process.destroy();
            try {
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
            }
        }
        LogUtils.info("===== shutdownLLaMAServer successful.");
    }

    public static String callLLaMAServer() throws Exception {
        callLLaMAServerDetection();
        List<Map<String, String>> messages = new ArrayList<>();
        ModelResMessage modelResMessage = sendChat(HomepageAdaptor.assembleMsg(messages));
        return assembleModelResMessage(modelResMessage);
    }

    public static String assembleModelResMessage(ModelResMessage modelResMessage) {
        String msg = "";
        if (modelResMessage.getContent() != null && !modelResMessage.getContent().isEmpty()) {
            msg = msg + modelResMessage.getContent();
        }
        if (modelResMessage.getReasoningContent() != null && !modelResMessage.getReasoningContent().isEmpty()) {
            msg = msg + "\n\n\n\n\n********************************************************************\n" +
                    "其中，AI思考过程：\n" + modelResMessage.getReasoningContent();
        }
        return msg;
    }


    public static void callLLaMAServerDetection() {
        for (int i = 0; i < 10; i++) {
            if (LLaMAConf.IS_LLAMA_SERVER_STARTED) {
                LogUtils.info("LLAMA_SERVER 已经启动！");
                break;
            } else {
                try {
                    Thread.sleep(1000);
                    LogUtils.info("等待 LLAMA_SERVER 启动...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static String getLLaMARealExecAbsPath() {
        HashMap<String, String> allConfigKeyValue = SysConfigAction.getAllConfigKeyValue();
        String llamaExecPath = LLaMAConf.getLLaMAExecAbsPath();
        // 使用自定义的llama路径，用户自行设定 cuda llama.ccp的路径
        if (!allConfigKeyValue.isEmpty() && !allConfigKeyValue.get("llama_custom_path").isEmpty()
                && !allConfigKeyValue.get("llama_custom_path").equals("*")) {
            llamaExecPath = allConfigKeyValue.get("llama_custom_path");
        }

        return llamaExecPath;
    }

    private static String assembleLLaMAParams(HashMap<String, String> allConfigKeyValue) {
        String ngl = "0";
        String temp = "0.6";
        String topK = "20";
        String topP = "0.95";
        String minP = "0.05";
        String presencePenalty = "1.2";
        String c = "16384";
        String n = "4096";
        for (String key : allConfigKeyValue.keySet()) {
            if ("-ngl".equals(key)) {
                ngl = allConfigKeyValue.get(key);
                // 如果启用了vulkan但是配置文件中没有做的对应的设定的，其中： -ngl -1 → 尽量把所有层都放 GPU（如果显存够的话）
                if (SysConfig.IS_USE_VULKAN){
                    if (Integer.parseInt(allConfigKeyValue.get(key)) == 0){
                        ngl = "-1";
                    }
                }
                continue;
            }
            if ("--temp".equals(key)) {
                temp = allConfigKeyValue.get(key);
                continue;
            }
            if ("--top-k".equals(key)) {
                topK = allConfigKeyValue.get(key);
                continue;
            }
            if ("--top-p".equals(key)) {
                topP = allConfigKeyValue.get(key);
                continue;
            }
            if ("--min-p".equals(key)) {
                minP = allConfigKeyValue.get(key);
                continue;
            }
            if ("--presence-penalty".equals(key)) {
                presencePenalty = allConfigKeyValue.get(key);
                continue;
            }
            if ("-c".equals(key)) {
                c = allConfigKeyValue.get(key);
                continue;
            }
            if ("-n".equals(key)) {
                n = allConfigKeyValue.get(key);
                continue;
            }
        }

        String llamaParams = "";
        if (!"*".equals(ngl)) {
            llamaParams = llamaParams + "-ngl " + ngl;
        }
        if (!"*".equals(temp)) {
            llamaParams = llamaParams + " " + "--temp " + temp;
        }
        if (!"*".equals(topK)) {
            llamaParams = llamaParams + " " + "--top-k " + topK;
        }
        if (!"*".equals(topP)) {
            llamaParams = llamaParams + " " + "--top-p " + topP;
        }
        if (!"*".equals(minP)) {
            llamaParams = llamaParams + " " + "--min-p " + minP;
        }
        if (!"*".equals(presencePenalty)) {
            llamaParams = llamaParams + " " + "--presence-penalty " + presencePenalty;
        }
        if (!"*".equals(c)) {
            llamaParams = llamaParams + " " + "-c " + c;
        }
        if (!"*".equals(n)) {
            llamaParams = llamaParams + " " + "-n " + n;
        }

        llamaParams = llamaParams + " --jinja --no-context-shift";
        return llamaParams;
    }


}
