package com.example.telegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegramBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(TelegramBotApplication.class, args);
    }
}

/** TODO: 03.09.2024
 * разобраться с токеном который надо подтверждать каждый час
 * добавить проверку наличия вопроса при его удалении
 * добавить кнопку отмены для каждого действия где требуется что то вводить
 * мб добавить исключение на смену роли у самого себя
 *
 *
 * ATTENTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * Реализовать вывод кодов владльцу бота для пользователей
 * Добавить кнопку "Карту заблочили" в которой будет ряд вопросов на которые надо будет ответить.
 * p.s. при тыканье на эту кнопку ответы записываются в отдельный общий лист.
  */
