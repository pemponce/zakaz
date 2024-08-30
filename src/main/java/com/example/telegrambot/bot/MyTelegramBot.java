package com.example.telegrambot.bot;

import com.example.telegrambot.command.AdminPanel;
import com.example.telegrambot.model.Message;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.BotMessageRepository;
import com.example.telegrambot.repository.MessageRepository;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.MessageService;
import com.example.telegrambot.service.QuestionsService;
import com.example.telegrambot.service.impl.UserServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;


@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botName;
    @Autowired
    private UserChatRepository userChatRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private BotMessageRepository botMessageRepository;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private QuestionsService questionsService;
    @Autowired
    private MessageService messageService;
    private Map<Long, String> userStates = new HashMap<>();



    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            Users currUser = userRepository.getUsersByUsername(update.getMessage().getFrom().getUserName());

            if (!(userChatRepository.existsByChatId(chatId) && userRepository.existsByChatId(chatId))) {
                userService.createUser(update);
            }

            // Получаем текущий статус пользователя
            String userState = userStates.getOrDefault(chatId, "");

            switch (userState) {
                case "WAITING_FOR_NEW_QUESTION":
                    // Логика добавления вопроса
                    questionsService.createQuestion(text);
                    sendMessage(chatId, "Вопрос \"" + text + "\" записан!");
                    userStates.remove(chatId); // Сбрасываем статус
                    break;
                case "WAITING_FOR_QUESTION_TO_DELETE":
                    // Логика удаления вопроса
//                    questionsService.deleteQuestion(text);
                    sendMessage(chatId, "Вопрос \"" + text + "\" удален!");
                    userStates.remove(chatId); // Сбрасываем статус
                    break;
                default:
                    if ("/admin".equals(text)) {
                        sendAdminPanel(chatId);
                    } else if ("Добавить вопрос".equals(text)) {

                        sendMessage(chatId, "Пожалуйста, введите новый вопрос:");
                        userStates.put(chatId, "WAITING_FOR_NEW_QUESTION");

                    } else if ("Вывести все вопросы".equals(text)) {

                        sendMessage(chatId, "Вот список всех вопросов:");
                        sendMessage(chatId, String.valueOf(questionsService.getAllQuestions()));

                    } else if ("Удалить вопрос".equals(text)) {

                        sendMessage(chatId, "Введите вопрос, который хотите удалить:");
                        userStates.put(chatId, "WAITING_FOR_QUESTION_TO_DELETE");

                    } else {
                        messageService.sendMessage(update, currUser);
                        sendMessage(chatId, "Ваш ответ записан\n```\n" + text + "\n```");
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
