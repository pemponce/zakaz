package com.example.telegrambot.bot;

import com.example.telegrambot.help.Mailing;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.BotMessageRepository;
import com.example.telegrambot.repository.MessageRepository;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.MessageService;
import com.example.telegrambot.service.impl.UserServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


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
    private MessageService messageService;


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            Users currUser = userRepository.getUsersByUsername(update.getMessage().getFrom().getUserName());

            if (!(userChatRepository.existsByChatId(chatId) && userRepository.existsByChatId(chatId))) {
                userService.createUser(update);
            }

            messageService.sendMessage(update, currUser);

            SendMessage message = new SendMessage();
            message.setText("Ваш ответ записан\n```\n" + update.getMessage().getText() + "\n```");
            message.setChatId(chatId.toString());

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
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
