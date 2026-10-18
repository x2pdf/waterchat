package com.logan.waterchat.chat.llamaccp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLaMARuntimeDiagnosticsTest {
    @Test
    void reportsMetalDeviceAndGpuLayers() {
        LLaMARuntimeDiagnostics diagnostics = new LLaMARuntimeDiagnostics();

        assertEquals("GPU 加速后端已初始化：Metal；设备=Apple M3 Pro",
                diagnostics.inspect("ggml_metal_init: found device 0: Apple M3 Pro"));
        assertEquals("模型 GPU 卸载完成：25/25 层",
                diagnostics.inspect("llama_model_load: offloaded 25/25 layers to GPU"));
        assertEquals("运行时加速状态：Metal GPU 已启用；设备=Apple M3 Pro；GPU 层=25/25 层",
                diagnostics.summary());
    }

    @Test
    void warnsWhenNoGpuBackendWasReported() {
        LLaMARuntimeDiagnostics diagnostics = new LLaMARuntimeDiagnostics();

        diagnostics.inspect("llama_model_loader: loaded meta data");

        assertTrue(diagnostics.summary().contains("可能使用 CPU"));
    }
}
