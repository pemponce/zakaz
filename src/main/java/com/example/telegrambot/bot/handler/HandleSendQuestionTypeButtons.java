package com.example.telegrambot.bot.handler;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.bot.telegramSender.TelegramSender;
import com.example.telegrambot.command.AdminPanel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@AllArgsConstructor
public class HandleSendQuestionTypeButtons {

    private final TelegramSender sender;
    public void sendQuestionTypeButtons(Long chatId, String action) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        String text = switch (action) {
            case "add" -> "Выберите тип данных для добавления:";
            case "delete" -> "Выберите тип данных для удаления:";
            case "list" -> "Выберите тип данных для отображения:";
            default -> "Ошибка!";
        };
        message.setText(text);
        message.setReplyMarkup(AdminPanel.questionAndAlertTypeButtons(action));

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
