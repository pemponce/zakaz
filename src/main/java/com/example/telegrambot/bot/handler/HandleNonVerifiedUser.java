package com.example.telegrambot.bot.handler;

import com.example.telegrambot.bot.panel.PanelSender;
import com.example.telegrambot.bot.state.StateService;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class HandleNonVerifiedUser {

    private final StateService stateService;
    private final UserService userService;
    private SendMessageService sendMessageService;
    private final PanelSender panelSender;

    public void handleNonVerifiedUser(String text, Long chatId, Users currUser) {
        String userState = stateService.getState(chatId);

        switch (userState) {
            case "AuthUser":
                if (userService.existsByVerificationCode(Integer.parseInt(text), currUser)) {
                    userService.verifyUser(Integer.parseInt(text), currUser);
                    sendMessageService.sendMessage(chatId, "Вы успешно авторизовались");
                    panelSender.sendGroupPanel(chatId);
                } else {
                    sendMessageService.sendMessage(chatId, "Код неверный, пожалуйста запросите код у администратора");
                    panelSender.sendAuthPanel(chatId);
                }
                break;
            default:
                if (text.equals("Авторизоваться")) {
                    sendMessageService.sendMessage(chatId, "Введите 4-значный код");
                    stateService.setState(chatId, "AuthUser");
                } else {
                    panelSender.sendAuthPanel(chatId);
                }
                break;
        }
    }

}
