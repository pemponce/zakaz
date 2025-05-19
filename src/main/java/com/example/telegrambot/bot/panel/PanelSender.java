package com.example.telegrambot.bot.panel;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.bot.telegramSender.TelegramSender;
import com.example.telegrambot.command.AdminPanel;
import com.example.telegrambot.command.AuthPanel;
import com.example.telegrambot.command.GroupPanel;
import com.example.telegrambot.command.UserPanel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@AllArgsConstructor
public class PanelSender {
    private final TelegramSender sender;
    public void sendAdminPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Выберите действие:");
        message.setReplyMarkup(AdminPanel.adminActions());

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendUserPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Если хотите увидеть истотрию уведомлений нажмите на кнопку");
        message.setReplyMarkup(UserPanel.userActions());
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void sendAuthPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Вы не авторизованы. Пожалуйста, авторизуйтесь:");
        message.setReplyMarkup(AuthPanel.authAction());

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendGroupPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Вы не указали свою группу, пожалуйста укажите ее");
        message.setReplyMarkup(GroupPanel.groupAction());

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            System.out.println("Ошибка при отправке сообщения: " + e.getMessage());
        }
    }

    public void sendCancelBtn(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Если хотите отменить, нажмите кнопку ниже:");
        message.setReplyMarkup(AdminPanel.adminCancelBtn());

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
