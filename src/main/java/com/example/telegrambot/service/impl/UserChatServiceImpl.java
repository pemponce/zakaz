package com.example.telegrambot.service.impl;

import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.service.UserChatService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserChatServiceImpl implements UserChatService {

    private final UserChatRepository userChatRepository;

    @Override
    public boolean existsByChatId(Long chatId) {
        return userChatRepository.existsByChatId(chatId);
    }

    @Override
    public UserChat getUserChatByChatId(Long chatId) {
        return userChatRepository.getUserChatByChatId(chatId);
    }

    @Override
    public List<UserChat> findAll() {
        return userChatRepository.findAll();
    }
}
