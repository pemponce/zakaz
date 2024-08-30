package com.example.telegrambot.help;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.quesionsEnum.Questions;
import com.example.telegrambot.repository.UserChatRepository;
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

    private int questionIndex = 1;

    @Scheduled(cron = "0 50 23 * * *")  // Запланировано на 13:40 каждый день
    public void sendDailyMessage() {
        if (questionIndex > Questions.values().length) {
            questionIndex = 1;
        }

        String question = Questions.getQuestionViaValue(questionIndex);
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
