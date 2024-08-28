package com.example.telegrambot.help;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.repository.UserChatRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class Mailing {
    @Autowired
    private MyTelegramBot myTelegramBot;

    @Autowired
    private UserChatRepository userChatRepository;


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
                if (now.getHour() == 4 && now.getMinute() == 9) {
                    broadcastMessage("Это ежедневное сообщение!");

                    this.cancel();
                    timer.cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 600);
    }

    public void broadcastMessage(String text) {
        List<UserChat> users = userChatRepository.findAll();
        for (UserChat user : users) {
            SendMessage message = new SendMessage();
            message.setChatId(user.getChatId().toString());
            message.setText(text);

            try {
                myTelegramBot.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
