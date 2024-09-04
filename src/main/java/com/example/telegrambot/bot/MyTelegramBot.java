package com.example.telegrambot.bot;

import com.example.telegrambot.command.AdminPanel;
import com.example.telegrambot.command.AuthPanel;
import com.example.telegrambot.googleSheets.service.GoogleSheetsService;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.model.enumRole.Role;
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
import java.util.*;


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
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        Users currUser = userRepository.getUsersByUsername(update.getMessage().getFrom().getUserName());

        if (update.hasMessage() && update.getMessage().hasText()) {


            if (!userChatRepository.existsByChatId(chatId) && !userRepository.existsByChatId(chatId)) {
                Users user = userService.createUser(update);

                try {
                    googleSheetsService.createSheet(spreadsheetId, user.getUsername());
                    sendWelcomeMessage(chatId);
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                    sendMessage(chatId, "Ошибка при создании листа в Google Sheets.");
                }
            }

            if (!userChatRepository.existsByChatId(chatId) && !userRepository.existsByChatId(chatId)) {
                Users user = userService.createUser(update);

                try {
                    googleSheetsService.createSheet(spreadsheetId, user.getUsername());
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                    sendMessage(chatId, "Ошибка при создании листа в Google Sheets.");
                }
            }

            if (currUser.isVerify()) {

                if (!Role.ADMIN.equals(currUser.getRole()) && text.equals("/admin")) {
                    sendMessage(chatId, "Вы не являетесь админом");
                }

                UserChat user = userChatRepository.getUserChatByChatId(chatId);
                List<Questions> questionsList = new ArrayList<>(questionsService.getAll());

                if (user.isWaitingForResponse()) {

                    messageService.saveMessage(update, currUser);
                    sendMessage(chatId, "Ваш ответ записан\n```\n" + text + "\n```");

                    long currentQuestionId = user.getCurrentQuestionId();
                    long maxId = questionsService.getMaxId();
                    long minId = questionsService.getMinId();

                    Questions nextQuestion = null;

                    for (Questions question : questionsList) {
                        if (question.getId() > currentQuestionId && question.getId() <= maxId) {
                            nextQuestion = question;
                            break;
                        }
                    }

                    if (nextQuestion != null) {
                        user.setCurrentQuestionId(nextQuestion.getId());
                        user.setWaitingForResponse(true);
                        userChatRepository.save(user);
                        sendMessage(chatId, nextQuestion.getQuestion());
                    } else {
                        user.setWaitingForResponse(false);
                        user.setCurrentQuestionId(minId);
                        userChatRepository.save(user);
                        sendMessage(chatId, "Спасибо! Вы ответили на все вопросы.");
                    }
                    return;
                }

                if (currUser.getRole().equals(Role.ADMIN)) {
                    if (Role.ADMIN.equals(currUser.getRole()) && text.equals("/admin")) {
                        sendAdminPanel(chatId);
                    }
                    if (text.startsWith("/setrole")) {
                        String[] parts = text.split(" ");
                        if (parts.length == 3) {
                            String username = parts[1];
                            String role = parts[2].toLowerCase();

                            if (userService.setRole(username, role)) {
                                sendMessage(chatId, "Пользователю " + username + " выдана роль " + role);
                            } else {
                                sendMessage(chatId, "Ошибка! Такого пользователя или роли не существует!");
                            }
                        } else {
                            sendMessage(chatId, "Неправильный формат команды. Используйте: /setrole username role");
                        }
                        return;
                    }
                    if (text.equals("/help")) {
                        sendMessage(chatId,
                                """
                                        /admin - команда доступная только админам. Команда выводит клавиатуру бота с функционалом
                                        /setrole - команда доступная только админам. Команда предназначена для смены роли другого пользователя, необходимо указать имя пользователя и роль которую хотите присвоить. Писать команду необходимо в таком формате\s
                                            <code>/setrole username role</code>\s
                                        где у роли есть 2 параметра (admin,user)
                                        """);
                    }

                    String userState = userStates.getOrDefault(chatId, "");
                    switch (userState) {
                        case "WAITING_FOR_NEW_QUESTION":
                            if (!text.equals("")) {
                                if (questionsService.createQuestion(text)) {
                                    sendMessage(chatId, "Вопрос \"" + text + "\" записан!");
                                } else {
                                    sendMessage(chatId, "Вопрос уже существует");
                                }
                            } else {
                                sendMessage(chatId, "Вопрос не может быть пустым");
                            }
                            userStates.remove(chatId); // Reset state
                            sendAdminPanel(chatId);

                            break;

                        case "WAITING_FOR_QUESTION_TO_DELETE":
                            questionsService.deleteQuestion(text);
                            sendMessage(chatId, "Вопрос \"" + text + "\" удален!");
                            userStates.remove(chatId);
                            sendAdminPanel(chatId);

                            break;

                        default:
                            switch (text) {
                                case "Добавить вопрос" -> {
                                    sendMessage(chatId, "Пожалуйста, введите новый вопрос:");
                                    userStates.put(chatId, "WAITING_FOR_NEW_QUESTION");
                                }
                                case "Вывести все вопросы" -> {
                                    sendMessage(chatId, "Вот список всех вопросов:");
                                    sendMessage(chatId, questionsService.getAllQuestions());
                                    sendAdminPanel(chatId);
                                }
                                case "Удалить вопрос" -> {
                                    sendMessage(chatId, "Введите вопрос, который хотите удалить:");
                                    userStates.put(chatId, "WAITING_FOR_QUESTION_TO_DELETE");
                                }
                                case "Вывести всех пользователей" -> {
                                    sendMessage(chatId, "Вот список всех пользователей:");
                                    sendMessage(chatId, userService.getAllUsers());
                                    sendAdminPanel(chatId);
                                }
                            }
                            break;
                    }
                } else {
                    sendMessage(chatId, "Дождитесь 23:50 чтобы ответить на вопросы");
                }
            } else {
                String userState = userStates.getOrDefault(chatId, "");
                switch (userState) {
                    case "AuthUser":
                        if (userService.existsByVerificationCode(Integer.parseInt(text), currUser)) {
                            userService.verifyUser(Integer.parseInt(text), currUser);
                            sendMessage(chatId, "Вы успешно авторизовались");

                        } else {
                            sendMessage(chatId, "Код неверный, пожалуйста запросите код у @...");
                            sendAuthPanel(chatId);
                        }
                        break;
                    default:
                        switch (text) {
                            case "Авторизоваться" -> {
                                sendMessage(chatId, "Введите 4х значный код");
                                userStates.put(chatId, "AuthUser");
                            }
                            default -> sendAuthPanel(chatId);
                        }
                        break;
                }
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

    private void sendAuthPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вам необходимо авторизироваться, запросите код у @...");
        message.setReplyMarkup(AuthPanel.authAction());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = "Привет! Добро пожаловать в нашего бота. Если вы являетесь админом то напишите /admin," +
                " после можете ознакомится с командами бота (/help)";
        sendMessage(chatId, welcomeText);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setParseMode("HTML");
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