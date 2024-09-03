package com.example.telegrambot.service;

import com.example.telegrambot.model.Users;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;


public interface UserService {
    Users createUser(Update update);
    String getAllUsers();
    boolean setRole(String username, String role);
}
