package com.example.telegrambot.service.impl;

import com.example.telegrambot.googleSheets.service.GoogleSheetsService;
import com.example.telegrambot.help.DateTimeFormatterExample;
import com.example.telegrambot.model.Message;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.MessageRepository;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.service.BanQuestionsService;
import com.example.telegrambot.service.MessageService;
import com.example.telegrambot.service.QuestionsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {

    private GoogleSheetsService googleSheetsService;
    private final MessageRepository messageRepository;
    private final QuestionsService questionsService;
    private final BanQuestionsService banQuestionsService;

    private final UserChatRepository userChatRepository;


    @Override
    public void saveMessage(Update update, ArrayList<Object> answers, Users currUser, boolean isBan) {
        String formattedTime = DateTimeFormatterExample.formatDateTime(LocalDateTime.now());

        // Получаем активный вопрос
        Long questionId = userChatRepository.getUserChatByChatId(currUser.getChatId()).getCurrentQuestionId();

        if (questionId != null) {
            String questionText;
            String range;

            if (!isBan) {
                questionText = questionsService.getQuestion(questionId).getQuestion();
                range = currUser.getUsername() + "!A1";
            } else {
                questionText = banQuestionsService.getQuestion(questionId).getQuestion();
                range = "cardBanned!B1";
            }

            Message saveMessage = Message.builder()
                    .userId(currUser.getId())
                    .chatId(currUser.getChatId())
                    .userName(currUser.getUsername())
                    .response_message(update.getMessage().getText())
                    .question(questionText)
                    .time(formattedTime)
                    .build();

            messageRepository.save(saveMessage);

            List<List<Object>> values;
            if (!isBan) {
                values = List.of(
                        List.of(saveMessage.getChatId().toString(), saveMessage.getResponse_message(),
                                saveMessage.getQuestion(), saveMessage.getTime(), saveMessage.getUserName(), saveMessage.getUserId())
                );
            } else {
                values = List.of(answers);
            }

            String spreadsheetId = "181N49nhhplDr52neZNqW_2O4d4Q9QwfXK4oEUsdt1l4"; // Укажи ID своей таблицы

            try {
                googleSheetsService.addDataToGoogleSheet(spreadsheetId, range, values);
                System.out.println("Сообщение отправлено в Google Sheets!");
            } catch (Exception e) {
                System.err.println("Ошибка при отправке данных в Google Sheets");
                e.printStackTrace();
            }

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
