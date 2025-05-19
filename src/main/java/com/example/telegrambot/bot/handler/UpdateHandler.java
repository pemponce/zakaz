package com.example.telegrambot.bot.handler;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.bot.telegramSender.TelegramSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@AllArgsConstructor
public class UpdateHandler {
    private final TelegramSender sender;
    private final HandleMessage handleMessage;
    private final HandleCallback handleCallback;

    public void handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage.handleMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallback.handleCallback(update);
        } else {
            System.out.println("Неизвестный тип обновления");
        }
    }

}
