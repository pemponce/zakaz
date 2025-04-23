package com.example.telegrambot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Emoji {
    ALERT("❗"),
    QUESTION("❓"),
    WARNING("⚠️"),
    ALARM("⏰"),
    CHECKED("✅");

    private final String data;

    public String toString() {
        return data;
    }
}
