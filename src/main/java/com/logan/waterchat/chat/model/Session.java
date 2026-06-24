package com.logan.waterchat.chat.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Session {
    private LocalDateTime time;
    private String title;
    private String subtitle;

    private ArrayList<MessageDTO> messageDTOS;
}
