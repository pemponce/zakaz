package com.example.telegrambot.bot.handler;

import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.service.MessageService;
import com.example.telegrambot.service.QuestionsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class HandleUserRequest {

    private final QuestionsService questionsService;
    private final MessageService messageService;
    private final SendMessageService sendMessageService;
    private final UserChatRepository userChatRepository;

    private final ArrayList<Object> answers = new ArrayList<>();


    public void userRequest(Update update, Long chatId, Users currUser, UserChat user, String text, @Nullable List<Questions> questionsList) {
        long currentQuestionId = user.getCurrentQuestionId();
        long maxId;
        long minId;
        boolean flag = false;

        Questions nextQuestion;
        maxId = questionsService.getMaxId();
        minId = questionsService.getMinId();

        nextQuestion = questionsList.stream().filter(q -> q.getId() > currentQuestionId && q.getId() <= maxId).findFirst().orElse(null);
        sendMessageService.sendMessage(chatId, "Ваш ответ записан\n<strong>" + text + "</strong>");

        messageService.saveMessage(update, null, currUser, flag);

        if (nextQuestion != null) {
            user.setCurrentQuestionId(nextQuestion.getId());
            sendMessageService.sendMessage(chatId, nextQuestion.getQuestion());

            user.setWaitingForResponse(true);
            userChatRepository.save(user);
        } else {
            user.setWaitingForResponse(false);
            user.setCurrentQuestionId(minId);
            userChatRepository.save(user);
            messageService.saveMessage(update, answers, currUser, flag);
            answers.clear();
            sendMessageService.sendMessage(chatId, "Спасибо! Вы ответили на все вопросы.");
        }
    }

}
