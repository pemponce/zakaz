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
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Mailing {

    @Autowired
    private MyTelegramBot myTelegramBot;
    @Autowired
    private UserChatRepository userChatRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private QuestionsServiceImpl questionsService;

    private static boolean status;

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendDailyMessage() {
        status = false;
        List<UserChat> chatUsers = userChatRepository.findAll();

        for (UserChat chatUser : chatUsers) {
            Long chatId = chatUser.getChatId();
            Users user = userRepository.getUsersByChatId(chatId);

            if (!user.getRole().equals(Role.ADMIN) && user.isVerify()) {
                broadcastMessage(chatId, "Пожалуйста ответьте на все вопросы");

                Questions currentQuestion = questionsService.findFirstByMorningFalse();
                chatUser.setCurrentQuestionId(currentQuestion.getId());
                chatUser.setWaitingForResponse(true);
                userChatRepository.save(chatUser);

                broadcastMessage(chatId, currentQuestion.getQuestion());

            } else {
                if (!user.isVerify()) {
                    broadcastMessage(chatId, "Авторизируйтесь! следующая рассылка будет в 10:30");

                } else {
                    broadcastMessage(chatId, "Рассылка началась");
                }
            }
        }
    }

    @Scheduled(cron = "0 30 10 * * *")
    public void sendMorningQuestions() {
        status = true;
        List<UserChat> chatUsers = userChatRepository.findAll();

        for (UserChat chatUser : chatUsers) {
            Long chatId = chatUser.getChatId();
            Users user = userRepository.getUsersByChatId(chatId);

            if (!user.getRole().equals(Role.ADMIN) && user.isVerify()) {
                broadcastMessage(chatId, "Пожалуйста ответьте на все вопросы");

                Questions currentQuestion = questionsService.findFirstByMorningTrue();
                chatUser.setCurrentQuestionId(currentQuestion.getId());
                chatUser.setWaitingForResponse(true);
                userChatRepository.save(chatUser);

                broadcastMessage(chatId, currentQuestion.getQuestion());

            } else {
                if (!user.isVerify()) {
                    broadcastMessage(chatId, "Авторизируйтесь! следующая рассылка будет в 10:30");

                } else {
                    broadcastMessage(chatId, "Рассылка началась");
                }
            }
        }
    }

    public static boolean morningQuestion() {
        return status;
    }

    public void broadcastMessage(Long chatId, String text) {
        executionWrapper(() -> {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);

            return myTelegramBot.execute(message);
        }, userRepository.getUsersByChatId(chatId).getUsername());
    }

    private interface BroadcastMessageExecution<T> {
        T execute() throws Exception;
    }

    private <T> T executionWrapper(BroadcastMessageExecution<T> execution, String username) {
        try {
            execution.execute();
            log.info("Сообщение отправлено пользователю {}", username);
        } catch (TelegramApiException err) {
            log.error("ошибка: пользователь " + username + " заблокировал бота\n {}", err.fillInStackTrace().getMessage());
        } catch (Exception e) {
            log.error("Can't execute request", e);
        }
        return null;
    }

}