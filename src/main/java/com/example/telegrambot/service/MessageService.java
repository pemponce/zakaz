package com.example.telegrambot.service;

import com.example.telegrambot.model.Users;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

public interface MessageService {
    void saveMessage(Update update, ArrayList<Object> answers, Users currUser, boolean isBan);
}
