package com.logan.waterchat.chat.model;


public class MessageDTO {

    private ChatRoleEnum role;
    private String content;


    public ChatRoleEnum getRole() {
        return role;
    }

    public void setRole(ChatRoleEnum role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
