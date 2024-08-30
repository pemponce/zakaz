package com.example.telegrambot.service.impl;

import com.example.telegrambot.help.DateTimeFormatterExample;
import com.example.telegrambot.model.Message;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.MessageRepository;
import com.example.telegrambot.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {

    private MessageRepository messageRepository;
    @Override
    public void sendMessage(Update update, Users currUser) {
        String formattedTime = DateTimeFormatterExample.formatDateTime(LocalDateTime.now());


        Message saveMessage = Message.builder()
                .userId(currUser.getId())
                .chatId(currUser.getChatId())
                .userName(currUser.getUsername())
                .response_message(update.getMessage().getText())
                .time(formattedTime)
                .build();

        messageRepository.save(saveMessage);
    }
}
