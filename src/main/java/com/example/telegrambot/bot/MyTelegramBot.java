package com.example.telegrambot.bot;

import com.example.telegrambot.bot.handler.UpdateHandler;
import com.example.telegrambot.bot.telegramSender.TelegramSender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botName;

    private final UpdateHandler updateHandler;

    public MyTelegramBot(@Value("${telegram.bot.token}") String botToken, UpdateHandler updateHandler) {
        super(botToken);
        this.updateHandler = updateHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateHandler.handle(update);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
        return super.execute(method);
    }
}
