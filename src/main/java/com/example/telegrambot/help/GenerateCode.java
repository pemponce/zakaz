package com.example.telegrambot.help;

import com.example.telegrambot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class GenerateCode {
    private final UserRepository userRepository;

    @Autowired
    public GenerateCode(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public int generateCode() {
        Random random = new Random();
        int code;
        do {
            code = 1000 + random.nextInt(9000); // Генерация 4-значного кода
        } while (userRepository.existsByVerificationCode(code)); // Проверка уникальности кода

        return code;
    }
}