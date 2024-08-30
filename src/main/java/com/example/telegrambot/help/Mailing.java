package com.example.telegrambot.help;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.service.impl.QuestionsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class Mailing {

    @Autowired
    private MyTelegramBot myTelegramBot;

    @Autowired
    private UserChatRepository userChatRepository;

    @Autowired
    private QuestionsServiceImpl questionsService;

    private long questionIndex = 1;

    @Scheduled(cron = "0 50 23 * * *")  // Запланировано на 13:40 каждый день
    public void sendDailyMessage() {
        if (questionIndex > questionsService.getQuestionsLength()) {
            questionIndex = 1;
        }

        String question = questionsService.getQuestion(questionIndex);
        questionIndex++;

        broadcastMessage(question);
    }

    public void broadcastMessage(String text) {
        List<UserChat> users = userChatRepository.findAll();
        for (UserChat user : users) {
            SendMessage message = new SendMessage();
            message.setChatId(user.getChatId().toString());
            message.setText(text);

            try {
                myTelegramBot.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
