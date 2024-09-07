package com.example.telegrambot.command;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class AdminPanel {
    public static ReplyKeyboard adminActions() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Добавить вопрос");
        row1.add("Удалить вопрос");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Вывести все вопросы");
        row2.add("Вывести всех пользователей");


        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }

    public static ReplyKeyboard adminCancelBtn() {
        KeyboardRow row = new KeyboardRow();
        row.add("Отмена");

        return getReplyKeyboard(row);
    }

    @NotNull
    static ReplyKeyboard getReplyKeyboard(KeyboardRow row) {
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        return keyboardMarkup;
    }
}
