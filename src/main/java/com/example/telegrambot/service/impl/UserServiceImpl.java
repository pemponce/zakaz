package com.example.telegrambot.service.impl;

import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserChatRepository userChatRepository;
    private UserRepository userRepository;

    @Override
    public Users createUser(Update update) {
        Long chatId = update.getMessage().getChatId();

        UserChat userChat = UserChat.builder().chatId(chatId).build();
        userChat.setChatId(chatId);
        userChatRepository.save(userChat);

        Users user = Users.builder()
                .username(update.getMessage().getFrom().getUserName())
                .chatId(chatId)
                .build();

        return userRepository.save(user);
    }
}
