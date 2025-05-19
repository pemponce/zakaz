package com.example.telegrambot.command;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class AdminPanel {
    public static ReplyKeyboard adminActions() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Добавить данные");
        row1.add("Удалить данные");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Вывести все данные");
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

    public static InlineKeyboardMarkup questionAndAlertTypeButtons(String action) {
        InlineKeyboardButton normalQuestionsBtn = new InlineKeyboardButton();
        normalQuestionsBtn.setText("Вопросы");
        normalQuestionsBtn.setCallbackData(action + "_normal");

        InlineKeyboardButton alertsBtn = new InlineKeyboardButton();
        alertsBtn.setText("Оповещения");
        alertsBtn.setCallbackData(action + "_info");

        List<InlineKeyboardButton> row = List.of(normalQuestionsBtn, alertsBtn);
        List<List<InlineKeyboardButton>> rows = List.of(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        return markup;
    }


    /*
    TODO: Сделать, кнопку подробной информации для админа о рассылках и вопросах (их содержимое)!
     */
    public static InlineKeyboardMarkup contentOfMailingButton(String typeInfo) {
        InlineKeyboardButton moreInfoButton = new InlineKeyboardButton();
        moreInfoButton.setText("Информация о рассылке");
        moreInfoButton.setCallbackData(typeInfo + "InformationContent");

        List<InlineKeyboardButton> row = List.of(moreInfoButton);
        List<List<InlineKeyboardButton>> rows = List.of(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

}
