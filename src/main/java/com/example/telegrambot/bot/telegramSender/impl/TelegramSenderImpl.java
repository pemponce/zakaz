package com.example.telegrambot.bot.telegramSender.impl;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.bot.telegramSender.TelegramSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@Component
public class TelegramSenderImpl implements TelegramSender {

    private final MyTelegramBot myTelegramBot;

    public TelegramSenderImpl(@Lazy MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
        return myTelegramBot.execute(method);
    }
}