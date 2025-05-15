package com.example.telegrambot.bot.handler;

import com.example.telegrambot.bot.panel.PanelSender;
import com.example.telegrambot.bot.telegramSender.TelegramSender;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@AllArgsConstructor
public class HandleMessage {
    private final TelegramSender sender;
    private final UserRepository userRepository;
    private final UserChatRepository userChatRepository;
    private final UserService userService;
    private final HandleVerifiedUser handleVerifiedUser;
    private final HandleNonVerifiedUser handleNonVerifiedUser;
    private final PanelSender panelSender;

    public void handleMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        Users currUser = userRepository.getUsersByUsername(update.getMessage().getFrom().getUserName());

        if (currUser == null) {
            if (!userChatRepository.existsByChatId(chatId) && !userRepository.existsByChatId(chatId)) {
                userService.createUser(update);

                sendWelcomeMessage(chatId);

            }
        } else {
            if (currUser.isVerify()) {
                handleVerifiedUser.handleVerifiedUser(text, chatId, currUser, update);

            } else {
                handleNonVerifiedUser.handleNonVerifiedUser(text, chatId, currUser);
            }
        }
    }


    public void sendWelcomeMessage(Long chatId) {
        String welcomeText = "Привет! Добро пожаловать в нашего бота. Если вы являетесь админом то напишите /admin," + " после можете ознакомится с командами бота (/help)";
        SendMessage message = new SendMessage(chatId.toString(), welcomeText);
        message.setText(welcomeText);
        message.setParseMode("HTML");
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        panelSender.sendAuthPanel(chatId);
    }

}
