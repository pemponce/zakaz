package com.example.telegrambot.service;

import com.example.telegrambot.model.Users;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;


public interface UserService {
    Users createUser(Update update);
    void updateUserGroup(String username, String group);
    String getAllUsers();
    Users getUsersByChatId(Long chatId);
    boolean setRole(String username, String role);
    boolean existsByVerificationCode(int code, Users currUser);
    void verifyUser(int code, Users currUser);
}
