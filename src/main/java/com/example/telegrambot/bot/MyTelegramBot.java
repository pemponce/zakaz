package com.example.telegrambot.bot;

import com.example.telegrambot.command.AdminPanel;
import com.example.telegrambot.googleSheets.service.GoogleSheetsService;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.MessageRepository;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.MessageService;
import com.example.telegrambot.service.QuestionsService;
import com.example.telegrambot.service.impl.UserServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;


@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    @Value("${google.sheets.spreadsheetId}")
    private String spreadsheetId;

    @Value("${telegram.bot.username}")
    private String botName;
    @Autowired
    private UserChatRepository userChatRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private QuestionsService questionsService;
    @Autowired
    private MessageService messageService;
    private final Map<Long, String> userStates = new HashMap<>();
    @Autowired
    private GoogleSheetsService googleSheetsService;



    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            Users currUser = userRepository.getUsersByUsername(update.getMessage().getFrom().getUserName());

            // Check if the user exists, if not, create and register them
            if (!userChatRepository.existsByChatId(chatId) && !userRepository.existsByChatId(chatId)) {
                Users user = userService.createUser(update);

                try {
                    googleSheetsService.createSheet(spreadsheetId, user.getUsername());
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                    sendMessage(chatId, "Ошибка при создании листа в Google Sheets.");
                }
            }

            // Admin panel handling
            if ("/admin".equals(text)) {
                sendAdminPanel(chatId);
                return;
            }

            // User state management (question-response handling)
            UserChat user = userChatRepository.getUserChatByChatId(chatId);
            if (user.isWaitingForResponse()) {
                // Save the user's response
                messageService.sendMessage(update, currUser);
                sendMessage(chatId, "Ваш ответ записан\n```\n" + text + "\n```");

                // Move to the next question or finish
                long nextQuestionIndex = user.getCurrentQuestionId() + 1;
                if (nextQuestionIndex <= questionsService.getQuestionsLength()) {
                    Questions nextQuestion = questionsService.getQuestion(nextQuestionIndex);
                    user.setCurrentQuestionId(nextQuestion.getId());
                    user.setWaitingForResponse(true);
                    userChatRepository.save(user);

                    // Send the next question
                    sendMessage(chatId, nextQuestion.getQuestion());
                } else {
                    // All questions answered
                    user.setWaitingForResponse(false);
                    user.setCurrentQuestionId(1L);
                    userChatRepository.save(user);
                    sendMessage(chatId, "Спасибо! Вы ответили на все вопросы.");
                }
                return;
            }

            // Admin functionality handling
            String userState = userStates.getOrDefault(chatId, "");
            switch (userState) {
                case "WAITING_FOR_NEW_QUESTION":
                    // Logic for adding a question
                    if (!text.equals("")) {
                        if (questionsService.createQuestion(text)) {
                            sendMessage(chatId, "Вопрос \"" + text + "\" записан!");
                        } else {
                            sendMessage(chatId, "Вопрос уже сужествует");
                        }
                    } else {
                        sendMessage(chatId, "Вопрос не может быть пустым");
                    }
                    userStates.remove(chatId); // Reset state
                    break;

                case "WAITING_FOR_QUESTION_TO_DELETE":
                    // Logic for deleting a question
//                    questionsService.delete(text);
                    questionsService.deleteQuestion(text);
                    sendMessage(chatId, "Вопрос \"" + text + "\" удален!");
                    userStates.remove(chatId); // Reset state
                    break;

                default:
                    if ("Добавить вопрос".equals(text)) {
                        sendMessage(chatId, "Пожалуйста, введите новый вопрос:");
                        userStates.put(chatId, "WAITING_FOR_NEW_QUESTION");
                    } else if ("Вывести все вопросы".equals(text)) {
                        sendMessage(chatId, "Вот список всех вопросов:");
                        sendMessage(chatId, questionsService.getAllQuestions().toString());
                    } else if ("Удалить вопрос".equals(text)) {
                        sendMessage(chatId, "Введите вопрос, который хотите удалить:");
                        userStates.put(chatId, "WAITING_FOR_QUESTION_TO_DELETE");
                    } else {
                        // Default handling for messages that aren't admin-related
                        sendMessage(chatId, "Команда не распознана. Пожалуйста, используйте доступные команды.");
                    }
                    break;
            }
        }
    }

    private void sendAdminPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Выберите действие:");
        message.setReplyMarkup(AdminPanel.adminActions());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public MyTelegramBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}