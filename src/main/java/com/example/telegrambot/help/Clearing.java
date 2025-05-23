package com.example.telegrambot.help;

import com.example.telegrambot.executors.Executor;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Role;
import com.example.telegrambot.service.QuestionsService;
import com.example.telegrambot.service.UserChatService;
import com.example.telegrambot.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class Clearing {

    private final QuestionsService questionsService;
    private final UserService userService;
    private final UserChatService userChatService;
    private Executor executor;

    @Scheduled(cron = "0 55 22 * * *")
    public void deleteAllDailyQuestions() {
        var usersChats = userChatService.findAll();
        List<Questions> relevantQuestions;
        if (!questionsService.getAllQuestionsByRelevantTrue().isEmpty()) {
            relevantQuestions = questionsService.getAllQuestionsByRelevantTrue();

            for (Questions relevantQuestion : relevantQuestions) {
                relevantQuestion.setRelevant(false);
                questionsService.save(relevantQuestion);
            }

            for (UserChat userChat : usersChats) {

                var chatId = userChat.getChatId();
                var user = userService.getUsersByChatId(chatId);
                if (user.getRole().equals(Role.ADMIN)) {

                    executor.broadcastMessage(chatId, "Список вопросов был удален!\nПожалуйста успейте заполнить новый" +
                            " список вопросов до 13:00 следущего дня\nИли не заполняйте если вопросов нет", Mailing.adminPanelExecute);
                }
            }

        } else {
            log.warn("Нет актуальных вопросов");
        }
    }
}
