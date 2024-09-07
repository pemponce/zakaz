package com.example.telegrambot.command;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.example.telegrambot.command.AdminPanel.getReplyKeyboard;

public class AuthPanel {
    public static ReplyKeyboard authAction() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Авторизоваться");

        return getReplyKeyboard(row1);
    }

}
