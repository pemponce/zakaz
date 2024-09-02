package com.example.telegrambot.help;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.MessageService;
import com.example.telegrambot.service.impl.QuestionsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
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
    @Scheduled(cron = "0 24 * * * *")
    public void sendDailyMessage() {
        long questionIndex = 1;
        List<UserChat> users = userChatRepository.findAll();
        for (UserChat user : users) {
            Long chatId = user.getChatId();
            Questions currentQuestion = questionsService.getQuestion(questionIndex);

            // Save the current question in the user's state
            user.setCurrentQuestionId(currentQuestion.getId());
            user.setWaitingForResponse(true);
            userChatRepository.save(user);

            // Broadcast the question
            broadcastMessage(chatId, currentQuestion.getQuestion());
        }
    }

    public void broadcastMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            myTelegramBot.execute(message);
            System.out.println("Сообщение отправлено пользователю: " + chatId); // Логирование отправки

        } catch (TelegramApiException e) {
            System.err.println("Ошибка при отправке сообщения пользователю: " + chatId); // Логирование ошибки
            e.printStackTrace();
        }
    }
}