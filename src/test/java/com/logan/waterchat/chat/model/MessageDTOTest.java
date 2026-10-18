package com.logan.waterchat.chat.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageDTOTest {
    @Test
    void fallsBackToRawContentUntilSpecializedContentIsSet() {
        MessageDTO message = new MessageDTO();
        message.setContent("raw");
        assertEquals("raw", message.getDisplayContent());
        assertEquals("raw", message.getModelContent());

        message.setDisplayContent("preview");
        message.setModelContent("compressed");
        assertEquals("raw", message.getContent());
        assertEquals("preview", message.getDisplayContent());
        assertEquals("compressed", message.getModelContent());
    }
}
