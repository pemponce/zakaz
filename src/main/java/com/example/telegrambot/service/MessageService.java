package com.example.telegrambot.service;

import com.example.telegrambot.model.Users;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface MessageService {
    void saveMessage(Update update, Users user);
}
