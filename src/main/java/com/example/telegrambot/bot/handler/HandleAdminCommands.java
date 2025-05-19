package com.example.telegrambot.bot.handler;

import com.example.telegrambot.bot.panel.PanelSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class HandleAdminCommands {

    private final PanelSender panelSender;
    private final HandleCommandsMessage commandsMessage;

    public void handleAdminCommands(String text, Long chatId) {
        String[] parts = text.split(" ");

        switch (parts[0]) {
            case "/admin" -> panelSender.sendAdminPanel(chatId);
            case "/setrole" -> commandsMessage.handleSetRoleCommand(text, chatId);
            case "/help" -> commandsMessage.sendHelpMessage(chatId);
            case "/setTime" -> commandsMessage.handleSetIsMorning(text, chatId);
            default -> commandsMessage.sendQuestionTypeButtonsBasedOnText(text, chatId);
        }
    }

}
