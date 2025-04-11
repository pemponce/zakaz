package com.example.telegrambot.help;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.executors.Executor;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.model.enumRole.Role;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.MessageService;
import com.example.telegrambot.service.UserService;
import com.example.telegrambot.service.impl.QuestionsServiceImpl;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class Mailing {

    private Executor executor;
    @Autowired
    private UserChatRepository userChatRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private QuestionsServiceImpl questionsService;

    private static boolean status;

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendDailyMessage() {
        status = false;
        List<UserChat> chatUsers = userChatRepository.findAll();

        for (UserChat chatUser : chatUsers) {
            Long chatId = chatUser.getChatId();
            Users user = userService.getUsersByChatId(chatId);
            if (!user.getRole().equals(Role.ADMIN) && user.isVerify() && questionsService.findFirstByMorningFalse(user.getUserGroup()) != null) {
                executor.broadcastMessage(chatId, "Пожалуйста ответьте на все вопросы");

                Questions currentQuestion = questionsService.findFirstByMorningFalse(user.getUserGroup());
                chatUser.setCurrentQuestionId(currentQuestion.getId());
                chatUser.setWaitingForResponse(true);
                userChatRepository.save(chatUser);

                executor.broadcastMessage(chatId, currentQuestion.getQuestion());

            } else {
                if (!user.isVerify()) {
                    executor.broadcastMessage(chatId, "Авторизируйтесь! следующая рассылка будет в 10:30");

                }
                if (user.isVerify() && questionsService.findFirstByMorningFalse(user.getUserGroup()) == null) {
                    executor.broadcastMessage(chatId, "Дневних вопросов сегодня нет");

                }else {
                    executor.broadcastMessage(chatId, "Рассылка началась");
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
            Users user = userService.getUsersByChatId(chatId);

            if (!user.getRole().equals(Role.ADMIN) && user.isVerify() && questionsService.findFirstByMorningTrue(user.getUserGroup()) != null) {
                executor.broadcastMessage(chatId, "Пожалуйста ответьте на все вопросы");

                Questions currentQuestion = questionsService.findFirstByMorningTrue(user.getUserGroup());
                chatUser.setCurrentQuestionId(currentQuestion.getId());
                chatUser.setWaitingForResponse(true);
                userChatRepository.save(chatUser);

                executor.broadcastMessage(chatId, currentQuestion.getQuestion());

            } else {
                if (!user.isVerify()) {
                    executor.broadcastMessage(chatId, "Авторизируйтесь! следующая рассылка будет в 10:30");

                }
                if (user.isVerify() && questionsService.findFirstByMorningTrue(user.getUserGroup()) == null) {
                    executor.broadcastMessage(chatId, "Утренних вопросов сегодня нет");

                } else {
                    executor.broadcastMessage(chatId, "Рассылка началась");
                }
            }
        }
    }

    public static boolean morningQuestion() {
        return status;
    }

}