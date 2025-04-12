package com.example.telegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramBotApplication.class, args);
    }
}

/*
  TODO: 03.09.2024
    - Реализовать отправку попвещения от куратора
    - Реализовать отправку ранее отправленных уведомлений
   - разобраться с токеном который надо подтверждать каждый час ⁉
   - добавить проверку наличия вопроса при его удалении ✅
   - мб добавить исключение на смену роли у самого себя ✅
   - ATTENTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   - Реализовать вывод кодов владльцу бота для пользователей ✅
   - реализовать настройку параметра в вопросе типа утренний или нет ✅️
   - https://docs.google.com/spreadsheets/d/181N49nhhplDr52neZNqW_2O4d4Q9QwfXK4oEUsdt1l4/edit?gid=1198020588#gid=1198020588
 */