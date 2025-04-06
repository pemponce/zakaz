package com.example.telegrambot.command;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import static com.example.telegrambot.command.AdminPanel.getReplyKeyboard;

public class GroupPanel {

        public static ReplyKeyboard groupAction() {
            KeyboardRow row1 = new KeyboardRow();
            row1.add("Указать группу");

            return getReplyKeyboard(row1);
        }



}
