package com.example.telegrambot.bot;

import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.repository.UserChatRepository;
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

    private Long chatId;

    @PostConstruct
    public void init() {
        sendDailyMessage();
    }

    public void sendDailyMessage() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                LocalTime now = LocalTime.now();
                if (now.getHour() == 1 && now.getMinute() == 3) {
                    broadcastMessage("Это ежедневное сообщение!");

                    this.cancel();
                    timer.cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 6000); // Проверяем каждую минуту
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();

            // Проверка и сохранение chatId
            if (!userChatRepository.existsByChatId(chatId)) {
                UserChat userChat = new UserChat();
                userChat.setChatId(chatId);
                userChatRepository.save(userChat);
            }

            // Создание и отправка ответа пользователю
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Ваш ответ записан\n```\n"
                    + update.getMessage().getText()
            +"\n```");

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMessage(String text) {
        List<UserChat> users = userChatRepository.findAll();
        for (UserChat user : users) {
            SendMessage message = new SendMessage();
            message.setChatId(user.getChatId().toString());
            message.setText(text);

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
