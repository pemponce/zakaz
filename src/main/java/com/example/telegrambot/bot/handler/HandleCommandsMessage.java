package com.example.telegrambot.bot.handler;

import com.example.telegrambot.bot.panel.PanelSender;
import com.example.telegrambot.service.QuestionsService;
import com.example.telegrambot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class HandleCommandsMessage {

    private final PanelSender panelSender;
    private final SendMessageService sendMessageService;
    private final QuestionsService questionsService;
    private final UserService userService;
    private final HandleSendQuestionTypeButtons handleSendQuestionTypeButtons;

    public void handleSetRoleCommand(String text, Long chatId) {
        String[] parts = text.split(" ");
        if (parts.length == 3) {
            String username = parts[1];
            String role = parts[2].toLowerCase();

            if (userService.setRole(username, role)) {
                sendMessageService.sendMessage(chatId, "Пользователю " + username + " выдана роль " + role);
            } else {
                sendMessageService.sendMessage(chatId, "Ошибка! Такого пользователя или роли не существует!");
            }
        } else {
            sendMessageService.sendMessage(chatId, "Неправильный формат команды. Используйте: /setrole username role");
        }
        panelSender.sendAdminPanel(chatId);
    }

    public void sendHelpMessage(Long chatId) {
        sendMessageService.sendMessage(chatId, """
                /admin - команда доступна только админам. Команда выводит клавиатуру бота с функционалом
                                
                /setrole - команда доступна только админам. Команда предназначена для смены роли другого пользователя, необходимо указать имя пользователя и роль которую хотите присвоить. Писать команду необходимо в таком формате\s
                <code>/setrole username role</code>\s
                где у роли есть 2 параметра (admin,user)
                                
                /setTime - команда доступна только админам. Команда предназначена для смены времени вывода определенного вопроса.
                Например если вы захотите изменить время(10:30 или 23:50) отправки какого либо вопроса, вы можете написать \n<code>/setTime question time</code>
                где question это сам вопрос, а time это параметр который имеет 2 состояния (день/вечер)
                вот пример команды если я захочу выполнить вывод вопроса в 10:30
                <code>/setTime как дела? день</code>
                P.S. вопрос (как дела?) будет появляться у пользователей в 10:30 т.к. я написал (день), если напишешь (вечер) то в 23:50
                """);
    }

    public void handleSetIsMorning(String text, Long chatId) {
        String[] parts = text.split(" ");

        String question = "";
        for (int i = 1; i < parts.length - 1; i++) {
            question += parts[i];
            if (i != parts.length - 2) {
                question += " ";
            }
        }
        String isMorning = parts[parts.length - 1].toLowerCase();

        if (questionsService.setIsMorning(question, isMorning)) {
            sendMessageService.sendMessage(chatId, "Вопросу " + question + " выдана роль " + isMorning);
        } else {
            sendMessageService.sendMessage(chatId, "Ошибка! Такого вопроса или параметра не существует!");
        }

        panelSender.sendAdminPanel(chatId);
    }

    public void sendQuestionTypeButtonsBasedOnText(String text, Long chatId) {
        switch (text) {
            case "Добавить данные" -> handleSendQuestionTypeButtons.sendQuestionTypeButtons(chatId, "add");
            case "Удалить данные" -> handleSendQuestionTypeButtons.sendQuestionTypeButtons(chatId, "delete");
            case "Вывести все данные" -> handleSendQuestionTypeButtons.sendQuestionTypeButtons(chatId, "list");
            case "Вывести всех пользователей" -> {
                sendMessageService.sendMessage(chatId, "Вот список всех пользователей:");
                sendMessageService.sendMessage(chatId, userService.getAllUsers());
            }
            default -> sendMessageService.sendMessage(chatId, "Неизвестная команда. Пожалуйста, используйте доступные команды.");
        }
    }

}
