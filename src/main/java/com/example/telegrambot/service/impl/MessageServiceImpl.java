package com.example.telegrambot.service.impl;

import com.example.telegrambot.googleSheets.service.GoogleSheetsService;
import com.example.telegrambot.help.DateTimeFormatterExample;
import com.example.telegrambot.model.Message;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.MessageRepository;
import com.example.telegrambot.service.MessageService;
import com.example.telegrambot.service.QuestionsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {

    private GoogleSheetsService googleSheetsService;
    private final MessageRepository messageRepository;
    private final QuestionsService questionsService;

    @Override
    public void sendMessage(Update update, Users currUser) {
        String formattedTime = DateTimeFormatterExample.formatDateTime(LocalDateTime.now());

        // Получаем активный вопрос
        Optional<Questions> activeQuestionOpt = questionsService.findActiveQuestion();

        if (activeQuestionOpt.isPresent()) {
            String questionText = activeQuestionOpt.get().getQuestion();

            Message saveMessage = Message.builder()
                    .userId(currUser.getId())
                    .chatId(currUser.getChatId())
                    .userName(currUser.getUsername())
                    .response_message(update.getMessage().getText())
                    .question(questionText)
                    .time(formattedTime)
                    .build();

            messageRepository.save(saveMessage);

            List<List<Object>> values = List.of(
                    List.of(saveMessage.getChatId().toString(), saveMessage.getResponse_message(),
                            saveMessage.getQuestion(), saveMessage.getTime(), saveMessage.getUserName(), saveMessage.getUserId())
            );

            String spreadsheetId = "181N49nhhplDr52neZNqW_2O4d4Q9QwfXK4oEUsdt1l4"; // Укажи ID своей таблицы
            String range = "A1";

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
