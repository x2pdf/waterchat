package com.logan.waterchat.chat.model;


public class MessageDTO {

    private ChatRoleEnum role;
    private String content;
    private String displayContent;
    private String modelContent;


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

    public String getDisplayContent() {
        return displayContent == null ? content : displayContent;
    }

    public void setDisplayContent(String displayContent) {
        this.displayContent = displayContent;
    }

    public String getModelContent() {
        return modelContent == null ? content : modelContent;
    }

    public void setModelContent(String modelContent) {
        this.modelContent = modelContent;
    }
}
