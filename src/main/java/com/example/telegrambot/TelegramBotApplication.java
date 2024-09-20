package com.example.telegrambot;

import com.example.telegrambot.googleSheets.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.security.GeneralSecurityException;

@SpringBootApplication
@EnableScheduling
public class TelegramBotApplication {

    @Value("${google.sheets.spreadsheetId}")
    private String spreadsheetId;

    @Autowired
    private GoogleSheetsService googleSheetsService;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TelegramBotApplication.class, args);
        TelegramBotApplication app = context.getBean(TelegramBotApplication.class);
        app.createSheets();
    }

    public void createSheets() {
        try {
            googleSheetsService.createSheet(spreadsheetId, "usersCode");
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Ошибка создании таблицы usersCode, возможно она уже существует.");
        }

        try {
            googleSheetsService.createSheet(spreadsheetId, "cardBanned");
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Ошибка создании таблицы cardBanned, возможно она уже существует.");
        }
    }
}

/** TODO: 03.09.2024
 *  - разобраться с токеном который надо подтверждать каждый час ⁉
 *  - добавить проверку наличия вопроса при его удалении ✅
 * мб добавить исключение на смену роли у самого себя ✅
 * ATTENTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * Реализовать вывод кодов владльцу бота для пользователей ✅
 * Добавить кнопку "Карту заблочили" в которой будет ряд вопросов на которые надо будет ответить.
 * p.s. при тыканье на эту кнопку ответы записываются в отдельный общий лист. ✅
 *  - реализовать настройку параметра в вопросе типа утренний или нет ✅️
 *  - попробовать вводить в таблицу по примеру как скинул ильхан ⁉️
 * 1. Сколько симок есть
 * 2. Сколько сберов живых
 * 3. Сколько денег в обороте (этот вопрос должен задаваться первым в 10:00 мск)
 * 4. Доход за сегодня
  */


//Please open the following address in your browser:
//  https://accounts.google.com/o/oauth2/auth?access_type=online&client_id=968406044541-i9jdrlhmu99s2vthtbeda2dv697jt23s.apps.googleusercontent.com&redirect_uri=http://localhost:8888/Callback&response_type=code&scope=https://www.googleapis.com/auth/spreadsheets