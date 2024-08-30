package com.example.telegrambot.service.impl;

import com.example.telegrambot.help.DateTimeFormatterExample;
import com.example.telegrambot.model.Message;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.MessageRepository;
import com.example.telegrambot.service.MessageService;
import com.example.telegrambot.service.QuestionsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final QuestionsService questionsService;

    @Override
    public void sendMessage(Update update, Users currUser) {
        String formattedTime = DateTimeFormatterExample.formatDateTime(LocalDateTime.now());

        // Получаем активный вопрос
        Optional<Questions> activeQuestionOpt = questionsService.findActiveQuestion();

        if (activeQuestionOpt.isPresent()) {
            String questionText = activeQuestionOpt.get().getQuestion();

            Message saveMessage = Message.builder()
                    .userId(currUser.getId())
                    .chatId(currUser.getChatId())
                    .userName(currUser.getUsername())
                    .response_message(update.getMessage().getText())
                    .question(questionText)
                    .time(formattedTime)
                    .build();

            messageRepository.save(saveMessage);
        } else {
            // Обработка случая, когда активный вопрос отсутствует
            // Например, можно сохранить сообщение без вопроса или отправить уведомление
            Message saveMessage = Message.builder()
                    .userId(currUser.getId())
                    .chatId(currUser.getChatId())
                    .userName(currUser.getUsername())
                    .response_message(update.getMessage().getText())
                    .question("")
                    .time(formattedTime)
                    .build();

            messageRepository.save(saveMessage);
        }
    }
}
