package com.example.telegrambot.bot.handler;

import com.example.telegrambot.bot.telegramSender.TelegramSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@AllArgsConstructor
public class SendMessageService {
    private final TelegramSender sender;

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setParseMode("HTML");
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}