package com.example.telegrambot.executors;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.command.AdminPanel;
import com.example.telegrambot.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@AllArgsConstructor
@Component
public class Executor {

    private final MyTelegramBot myTelegramBot;
    private final UserService userService;


    public void broadcastMessage(Long chatId, String text, Boolean adminPanelExecute) {
        broadcastMessage(chatId, text, adminPanelExecute, null);
    }


        public void broadcastMessage(Long chatId, String text, Boolean adminPanelExecute, String type) {
        executionWrapper(() -> {
            SendMessage message = new SendMessage();
            message.setParseMode("HTML");
            message.setChatId(chatId.toString());
            message.setText(text);
            if (adminPanelExecute) {
                message.setReplyMarkup(AdminPanel.contentOfMailingButton(type));
            }

            return myTelegramBot.execute(message);
        }, userService.getUsersByChatId(chatId).getUsername());
    }
    private interface BroadcastMessageExecution<T> {
        T execute() throws Exception;
    }

    private <T> T executionWrapper(BroadcastMessageExecution<T> execution, String username) {
        try {
            execution.execute();
            log.info("Сообщение отправлено пользователю {}", username);
        } catch (TelegramApiException err) {
            log.error("ошибка: пользователь " + username + " заблокировал бота\n {}", err.fillInStackTrace().getMessage());
        } catch (Exception e) {
            log.error("Can't execute request", e);
        }
        return null;
    }
}
