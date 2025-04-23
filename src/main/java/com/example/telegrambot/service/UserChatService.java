package com.example.telegrambot.service;

import com.example.telegrambot.model.UserChat;

import java.util.List;

public interface UserChatService {

    boolean existsByChatId(Long chatId);
    UserChat getUserChatByChatId(Long chatId);
    List<UserChat> findAll();

}
