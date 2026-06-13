package com.logan.waterchat.chat;

public class AIModelNameDTO {

    // Qwen3.5-0.8B-GGUF/Qwen3.5-0.8B-Q8_0.gguf
    public String name;
    // Qwen3.5-0.8B-GGUF/mmproj-Qwen3.5-0.8B-BF16.gguf
    public String nameMmproj;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameMmproj() {
        return nameMmproj;
    }

    public void setNameMmproj(String nameMmproj) {
        this.nameMmproj = nameMmproj;
    }
}
