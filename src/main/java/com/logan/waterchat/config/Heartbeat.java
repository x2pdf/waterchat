package com.logan.waterchat.config;
import com.logan.waterchat.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Heartbeat {

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    public static void start() {
        scheduler.scheduleAtFixedRate(() -> {
            LogUtils.info("WaterChatApplication Heartbeat...");
        }, 0, 60, TimeUnit.SECONDS);
    }

    public static void stop() {
        scheduler.shutdown();
    }
}