package com.example.telegrambot.bot.handler;

import com.example.telegrambot.bot.state.StateService;
import com.example.telegrambot.googleSheets.service.GoogleSheetsService;
import com.example.telegrambot.model.Group;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.service.GroupService;
import com.example.telegrambot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class HandleSetUserGroup {

    private final GoogleSheetsService googleSheetsService;
    private final GroupService groupService;
    private final UserService userService;
    private final SendMessageService sendMessageService;
    private final StateService stateService;

    public void handleSetUserGroup(String text, Long chatId, Users currUser) {
        try {
            String groupSpreadsheetId = googleSheetsService.createSpreadsheetForGroup(text);


            if (groupService.findByName(text).isEmpty()) {
                Group group = Group.builder().name(text).spreadsheetId(groupSpreadsheetId).build();

                groupService.create(group);
                userService.updateUserGroup(currUser.getUsername(), group);
                googleSheetsService.createList(currUser.getUsername(), groupSpreadsheetId);
                sendMessageService.sendMessage(chatId, "Создана таблица для группы " + text + "\nДобавлен лист для " + currUser.getUsername());
            } else {
                userService.updateUserGroup(currUser.getUsername(), groupService.getByName(text));
                googleSheetsService.createList(currUser.getUsername(), groupSpreadsheetId);
                sendMessageService.sendMessage(chatId, "Добавлен лист для " + currUser.getUsername());
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendMessageService.sendMessage(chatId, "Ошибка при создании таблицы в Google Sheets.");
        }
        stateService.clearState(chatId);
    }

}
