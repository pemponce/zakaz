package com.example.telegrambot.help;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.model.enumRole.Role;
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
    private UserRepository userRepository;
    @Autowired
    private QuestionsServiceImpl questionsService;

    @Scheduled(cron = "0 0/4 * * * *")
    public void sendDailyMessage() {
        long questionIndex = 1;
        List<UserChat> chatUsers = userChatRepository.findAll();
        for (UserChat chatUser : chatUsers) {
            Long chatId = chatUser.getChatId();
            Users user = userRepository.getUsersByChatId(chatId);

            if (!user.getRole().equals(Role.ADMIN)) {
                broadcastMessage(chatId, "Пожалуйста ответьте на все вопросы");


                Questions currentQuestion = questionsService.getQuestion(questionIndex);

                // Save the current question in the user's state
                chatUser.setCurrentQuestionId(currentQuestion.getId());
                chatUser.setWaitingForResponse(true);
                userChatRepository.save(chatUser);

                // Broadcast the question
                broadcastMessage(chatId, currentQuestion.getQuestion());
            } else {
                String text = ("Рассылка началась");
                broadcastMessage(chatId, text);
            }
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