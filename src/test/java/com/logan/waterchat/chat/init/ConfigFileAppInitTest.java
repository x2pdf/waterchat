package com.logan.waterchat.chat.init;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigFileAppInitTest {
    @Test
    void migratesFormerDefaultContextSettings(@TempDir Path tempDir) throws Exception {
        Path config = tempDir.resolve("config.properties");
        Files.writeString(config, "-c=5000\n-n=1024\n", StandardCharsets.UTF_8);

        ConfigFileAppInit.initSystemConfigValue(config.toString());

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        assertEquals("32768", properties.getProperty("-c"));
        assertEquals("2048", properties.getProperty("-n"));
        assertEquals("2", properties.getProperty("config_schema_version"));
        assertEquals("24000", properties.getProperty("long_text_chunk_tokens"));
        assertEquals("20", properties.getProperty("long_text_max_chunks"));
        assertEquals("600", properties.getProperty("chat_request_timeout_seconds"));
    }
}
