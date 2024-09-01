package com.example.telegrambot.help;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.model.Questions;
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

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendDailyMessage() {

        if (questionIndex > 1) {
            Questions prevQuestion = questionsService.getQuestion(questionIndex - 1);
            prevQuestion.setActive(false);
            questionsService.saveQuestion(prevQuestion);
        } else {
            Questions prevQuestion = questionsService.getQuestion(questionsService.getQuestionsLength());
            prevQuestion.setActive(false);
            questionsService.saveQuestion(prevQuestion);
        }
        Questions currentQuestion = questionsService.getQuestion(questionIndex);
        currentQuestion.setActive(true);

        // Отправляем сообщение
        String question = currentQuestion.getQuestion();
        broadcastMessage(question);

        questionIndex++;

        if (questionIndex > questionsService.getQuestionsLength()) {
            questionIndex = 1;
        }
        questionsService.saveQuestion(currentQuestion);
    }


    public void broadcastMessage(String text) {
        List<UserChat> users = userChatRepository.findAll();
        for (UserChat user : users) {
            SendMessage message = new SendMessage();
            message.setChatId(user.getChatId().toString());
            message.setText(text);

            try {
                myTelegramBot.execute(message);
                System.out.println("Сообщение отправлено пользователю: " + user.getChatId()); // Логирование отправки

            } catch (TelegramApiException e) {
                System.err.println("Ошибка при отправке сообщения пользователю: " + user.getChatId()); // Логирование ошибки

                e.printStackTrace();
            }
        }
    }
}
