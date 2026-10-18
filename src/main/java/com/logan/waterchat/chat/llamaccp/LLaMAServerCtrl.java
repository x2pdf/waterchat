package com.logan.waterchat.chat.llamaccp;


import com.logan.waterchat.chat.model.MessageResDTO;
import com.logan.waterchat.chat.longtext.LongTextAnalyzer;
import com.logan.waterchat.chatui.HomepageAdaptor;
import com.logan.waterchat.config.SysConfig;
import com.logan.waterchat.config.SysConfigAction;
import com.logan.waterchat.chatui.AlertUtils;
import com.logan.waterchat.utils.LogUtils;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class LLaMAServerCtrl {
    public static Process process;
    private static volatile AtomicBoolean processStopRequested = new AtomicBoolean(false);
    private static final String BASE_URL = LLaMAConf.LLAMA_SERVER_BASE_URL;
    private static String CURRENT_MODEL = LLaMAConf.LLAMA_SERVER_BASE_MODEL;
    private static void refreshCurrentModel(){
        CURRENT_MODEL = SysConfig.MODEL_NAME;
    }

    public static void startLLaMAServer() {
        long startupStartNanos = System.nanoTime();
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

            File executable = new File(llamaExecPath, "llama-server");
            File model = new File(SysConfig.TEMP_RESOURCES_PATH + "models/" + CURRENT_MODEL);
            logLifecycle("启动前检查：exec=" + executable.getAbsolutePath()
                    + "，execExists=" + executable.isFile() + "，execCanExecute=" + executable.canExecute()
                    + "，model=" + model.getAbsolutePath() + "，modelExists=" + model.isFile()
                    + "，modelBytes=" + (model.isFile() ? model.length() : -1));

            LogUtils.info("llama.ccp command: " + command2);
            LogUtils.log2LocalLogFile("llama.ccp command: " + command2);
            String accelerationPlan = "推理加速配置：os=" + System.getProperty("os.name")
                    + "；arch=" + System.getProperty("os.arch")
                    + "；gpu-layers=" + allConfigKeyValue.getOrDefault("-ngl", "0")
                    + (System.getProperty("os.name").toLowerCase().contains("mac")
                    ? "；预期后端=Metal" : "");
            LogUtils.info(accelerationPlan);
            LogUtils.log2LocalLogFile(accelerationPlan);
            LLaMARuntimeDiagnostics runtimeDiagnostics = new LLaMARuntimeDiagnostics();
            long processCreateStartNanos = System.nanoTime();
            Process launchedProcess = Runtime.getRuntime().exec(command2);
            AtomicBoolean stopRequested = new AtomicBoolean(false);
            process = launchedProcess;
            processStopRequested = stopRequested;
            long processPid = launchedProcess.pid();
            logLifecycle("llama-server 进程已创建：pid=" + processPid + "，耗时="
                    + elapsedMillis(processCreateStartNanos) + "ms");
            AtomicBoolean readyLogged = new AtomicBoolean(false);
            launchedProcess.onExit().thenAccept(exited -> {
                String exitMessage = "llama-server 进程退出：pid=" + processPid
                        + "，exitCode=" + exited.exitValue()
                        + "，shutdownRequested=" + stopRequested.get();
                LogUtils.error(exitMessage);
                LogUtils.log2LocalLogFile(exitMessage);
                if (process == exited) {
                    LLaMAConf.IS_LLAMA_SERVER_STARTED = false;
                }
            });

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
                        String diagnostic = runtimeDiagnostics.inspect(line);
                        if (diagnostic != null) {
                            LogUtils.info(diagnostic);
                            LogUtils.log2LocalLogFile(diagnostic);
                        }
                        if (line.contains("server is listening on")
                                || line.contains("starting the main loop")
                                || line.contains(LLaMAConf.LLAMA_SERVER_HOST + ":" + LLaMAConf.LLAMA_SERVER_PORT)) {
                            LogUtils.info("IS_LLAMA_SERVER_STARTED: true");
                            if (readyLogged.compareAndSet(false, true)) {
                                String accelerationSummary = runtimeDiagnostics.summary();
                                LogUtils.info(accelerationSummary);
                                LogUtils.log2LocalLogFile(accelerationSummary);
                                logLifecycle("llama-server 已监听：pid=" + processPid + "，启动总耗时="
                                        + elapsedMillis(startupStartNanos) + "ms");
                            }
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
            String error = "startLLaMAServer Exception：耗时=" + elapsedMillis(startupStartNanos)
                    + "ms，异常=" + e;
            LogUtils.error(error);
            LogUtils.log2LocalLogFile(error);
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
        logLifecycle("收到 llama-server 重启请求");
        LLaMAServerCtrl.shutdownLLaMAServer();
        LLaMAServerCtrl.startLLaMAServer();
    }


    public static void shutdownLLaMAServer() {
        long shutdownStartNanos = System.nanoTime();
        LLaMAConf.IS_LLAMA_SERVER_STARTED = false;
        if (process != null) {
            processStopRequested.set(true);
            long pid = process.pid();
            logLifecycle("开始停止 llama-server：pid=" + pid + "，alive=" + process.isAlive());
            process.destroy();
            try {
                if (!process.waitFor(8, TimeUnit.SECONDS)) {
                    logLifecycle("llama-server 正常停止超时，执行强制停止：pid=" + pid);
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LogUtils.error("等待 llama-server 停止时线程被中断：pid=" + pid);
                process.destroyForcibly();
            }
        }
        logLifecycle("llama-server 停止流程完成：耗时=" + elapsedMillis(shutdownStartNanos) + "ms");
    }


    public static String callLLaMAServer() throws Exception {
        return callLLaMAServer(null, ignored -> { }, () -> false);
    }

    public static String callLLaMAServer(String currentMessage, Consumer<String> progress,
                                         BooleanSupplier cancelled) throws Exception {
        long callStartNanos = System.nanoTime();
        try {
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
        } finally {
            logLifecycle("本次 llama 调用结束：总耗时=" + elapsedMillis(callStartNanos) + "ms");
        }
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
        long startNanos = System.nanoTime();
        int attempts = 0;
        logLifecycle("开始等待 llama-server 就绪：timeout=10s");
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadlineNanos) {
            attempts++;
            if (process == null || !process.isAlive()) {
                LogUtils.error("llama-server 就绪检查失败：进程未运行，attempt=" + attempts);
                throw new IllegalStateException("llama-server 进程未运行");
            }
            if (AppHttpUtils.isLlamaServerHealth()) {
                LLaMAConf.IS_LLAMA_SERVER_STARTED = true;
                logLifecycle("llama-server 已通过健康检查：attempts=" + attempts + "，耗时="
                        + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos) + "ms");
                return;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("等待 llama-server 启动时被中断", e);
            }
        }
        LogUtils.error("等待 llama-server 启动超时：attempts=" + attempts + "，pid="
                + (process == null ? "none" : process.pid()));
        throw new IllegalStateException("等待 llama-server 启动超时");
    }

    private static void logLifecycle(String message) {
        String log = "[llama-lifecycle] " + message;
        LogUtils.info(log);
        LogUtils.log2LocalLogFile(log);
    }

    private static long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}
