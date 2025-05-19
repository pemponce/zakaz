package com.example.telegrambot.bot.telegramSender;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

public interface TelegramSender {
    <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException;
}