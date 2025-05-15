package com.example.telegrambot.bot.state;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StateService {
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();

    public void setState(Long chatId, String state) {
        userStates.put(chatId, state);
    }

    public String getState(Long chatId) {
        return userStates.getOrDefault(chatId, "");
    }

    public void clearState(Long chatId) {
        userStates.remove(chatId);
    }
}
