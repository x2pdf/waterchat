package com.logan.waterchat.chat.llamaccp;


import com.logan.waterchat.chat.model.MessageResDTO;
import com.logan.waterchat.chat.longtext.LongTextAnalyzer;
import com.logan.waterchat.chatui.HomepageAdaptor;
import com.logan.waterchat.config.SysConfig;
import com.logan.waterchat.config.SysConfigAction;
import com.logan.waterchat.chatui.AlertUtils;
import com.logan.waterchat.utils.LogUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class LLaMAServerCtrl {
    public static Process process;
    private static final String BASE_URL = LLaMAConf.LLAMA_SERVER_BASE_URL;
    private static String CURRENT_MODEL = LLaMAConf.LLAMA_SERVER_BASE_MODEL;
    private static void refreshCurrentModel(){
        CURRENT_MODEL = SysConfig.MODEL_NAME;
    }

    public static void startLLaMAServer() {
        try {
            LLaMAConf.IS_LLAMA_SERVER_STARTED = false;
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

            if (LLaMAConf.IS_MODEL_CONTAIN_MMPORJ_FILE){
                command2 = command2 + " --mmproj " + SysConfig.TEMP_RESOURCES_PATH + "models/" + LLaMAConf.MODEL_MMPORJ_FILENAME;
            }

            String lLaMAParams = LLaMAParams.assembleLLaMAParams(allConfigKeyValue);
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
                        } else {
                            LogUtils.info("llama output msg: " + line);
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


    public static void refreshModelMmprojInfo() {
        String mmprojValue = SysConfig.configHashMap.get(SysConfig.MODEL_NAME + ":mmproj");
        if ( mmprojValue == null){
            LLaMAConf.IS_MODEL_CONTAIN_MMPORJ_FILE = false;
            LLaMAConf.MODEL_MMPORJ_FILENAME = "";
        }else {
            LLaMAConf.IS_MODEL_CONTAIN_MMPORJ_FILE = true;
            LLaMAConf.MODEL_MMPORJ_FILENAME = mmprojValue;
        }
    }


    public static void restartLLaMAServer() {
        LogUtils.info("restartLLaMAServer");
        LLaMAServerCtrl.shutdownLLaMAServer();
        LLaMAServerCtrl.startLLaMAServer();
    }


    public static void shutdownLLaMAServer() {
        LLaMAConf.IS_LLAMA_SERVER_STARTED = false;
        if (process != null) {
            process.destroy();
            try {
                if (!process.waitFor(8, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
            }
        }
        LogUtils.info("===== shutdownLLaMAServer successful.");
    }


    public static String callLLaMAServer() throws Exception {
        return callLLaMAServer(null, ignored -> { }, () -> false);
    }

    public static String callLLaMAServer(String currentMessage, Consumer<String> progress,
                                         BooleanSupplier cancelled) throws Exception {
        callLLaMAServerDetection();
        if (process == null || !process.isAlive()){
            String llamaServerRunningErrorNowMsg = "应用（server）运行出错。请退出应用，然后重新打开应用。敬请谅解！" +
                    "\n\nThe application (server) encountered an error. " +
                    "Please exit the application and then reopen it. " +
                    "We apologize for the inconvenience!";
            LogUtils.error("LLaMAServer 运行出错了。请退出应用，然后重新打开应用。");
            return llamaServerRunningErrorNowMsg;
        }
        if (currentMessage != null
                && AppHttpUtils.countTokens(currentMessage) > SysConfig.LONG_TEXT_CHUNK_TOKENS) {
            HomepageAdaptor.compressLatestUserMessageForModel(currentMessage);
            return LongTextAnalyzer.analyze(currentMessage, progress, cancelled);
        }
        List<Map<String, String>> messages = new ArrayList<>();
        HomepageAdaptor.assembleMsg(messages);
        trimHistoryToBudget(messages);
        MessageResDTO messageResDTO = AppHttpUtils.sendChat(messages);
        return assembleModelResMessage(messageResDTO);
    }

    private static void trimHistoryToBudget(List<Map<String, String>> messages) {
        while (messages.size() > 1
                && AppHttpUtils.countTokens(joinMessageContent(messages)) > SysConfig.LONG_TEXT_CHUNK_TOKENS) {
            int removeIndex = "system".equals(messages.get(0).get("role")) && messages.size() > 2 ? 1 : 0;
            if (removeIndex >= messages.size() - 1) {
                break;
            }
            messages.remove(removeIndex);
            LogUtils.info("上下文超出预算，已从本次请求中省略一条较早消息");
        }
    }

    private static String joinMessageContent(List<Map<String, String>> messages) {
        StringBuilder content = new StringBuilder();
        for (Map<String, String> message : messages) {
            content.append(message.get("role")).append(':')
                    .append(message.get("content")).append('\n');
        }
        return content.toString();
    }


    public static String assembleModelResMessage(MessageResDTO messageResDTO) {
        String msg = "";
        if (messageResDTO.getContent() != null && !messageResDTO.getContent().isEmpty()) {
            msg = msg + messageResDTO.getContent();
        }
        if (messageResDTO.getReasoningContent() != null && !messageResDTO.getReasoningContent().isEmpty()) {
            msg = msg + "\n\n\n\n\n********************************************************************\n" +
                    "其中，AI思考过程：\n" + messageResDTO.getReasoningContent();
        }
        return msg;
    }


    public static void callLLaMAServerDetection() {
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadlineNanos) {
            if (process == null || !process.isAlive()) {
                throw new IllegalStateException("llama-server 进程未运行");
            }
            if (AppHttpUtils.isLlamaServerHealth()) {
                LLaMAConf.IS_LLAMA_SERVER_STARTED = true;
                LogUtils.info("LLAMA_SERVER 已经启动并通过健康检查！");
                return;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("等待 llama-server 启动时被中断", e);
            }
        }
        throw new IllegalStateException("等待 llama-server 启动超时");
    }
}
