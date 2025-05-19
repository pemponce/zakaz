package com.example.telegrambot.bot.handler;

import com.example.telegrambot.bot.panel.PanelSender;
import com.example.telegrambot.bot.state.StateService;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.AlertsService;
import com.example.telegrambot.service.QuestionsService;
import com.example.telegrambot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@AllArgsConstructor
public class HandleCallback {

    private final PanelSender panelSender;
    private final StateService state;
    private final SendMessageService sendMessageService;
    private final QuestionsService questionsService;
    private final AlertsService alertsService;
    private final UserRepository userRepository;
    private final UserService userService;


    public void handleCallback(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        String command = update.getCallbackQuery().getMessage().toString();

        switch (callbackData) {
            case "add_normal" -> {
                panelSender.sendCancelBtn(chatId);
                state.setState(chatId, "WAITING_FOR_NEW_QUESTION");
                sendMessageService.sendMessage(chatId, "Пожалуйста, введите новый вопрос:");
            }
            case "add_info" -> {
                panelSender.sendCancelBtn(chatId);
                state.setState(chatId, "WAITING_FOR_NEW_BAN_QUESTION");
                sendMessageService.sendMessage(chatId, "Пожалуйста, введите новое обьявление/оповещение:");
            }
            case "delete_normal" -> {
                panelSender.sendCancelBtn(chatId);
                state.setState(chatId, "WAITING_FOR_QUESTION_TO_DELETE");
                sendMessageService.sendMessage(chatId, "Введите обычный вопрос, который хотите удалить:");
            }
            case "delete_info" -> {
                panelSender.sendCancelBtn(chatId);
                state.setState(chatId, "WAITING_FOR_BAN_QUESTION_TO_DELETE");
                sendMessageService.sendMessage(chatId, "Введите обьявление/оповещение, которое хотите удалить:");
            }
            case "list_normal" -> {
                sendMessageService.sendMessage(chatId, "Вот список всех вопросов:");
                sendMessageService.sendMessage(chatId, questionsService.getAllQuestionsContent(userRepository.getUsersByChatId(chatId).getGroup().getName()));
                panelSender.sendAdminPanel(chatId);
            }
            case "list_info" -> {
                sendMessageService.sendMessage(chatId, "Вот список всех оповещений:");
                sendMessageService.sendMessage(chatId, alertsService.getAllAlertsContent(userRepository.getUsersByChatId(chatId).getGroup().getName()));
                panelSender.sendAdminPanel(chatId);
            }
            case "morningInformationContent" -> {
                sendMessageService.sendMessage(chatId, "Информация о утренней рассылки:");
                sendMessageService.sendMessage(chatId, questionsService.questionListToString(questionsService
                        .getMorningQuestions(userRepository.getUsersByChatId(chatId).getGroup().getName())));
                panelSender.sendAdminPanel(chatId);
            }
            case "dailyInformationContent" -> {
                sendMessageService.sendMessage(chatId, "Информация о дневной рассылки:");
                sendMessageService.sendMessage(chatId, questionsService.questionListToString(questionsService
                        .getNotMorningQuestions(userRepository.getUsersByChatId(chatId).getGroup().getName())));
                panelSender.sendAdminPanel(chatId);
            }
            case "alertInformationContent" -> {
                sendMessageService.sendMessage(chatId, "Информация об оповещении/обьявлении:");
                sendMessageService.sendMessage(chatId, alertsService.getLastGroupAlert(userRepository.getUsersByChatId(chatId).getGroup().getName()).getContent());
                panelSender.sendAdminPanel(chatId);
            }
            default -> {
                if ("list_users".equals(command)) {
                    sendMessageService.sendMessage(chatId, "Вот список всех пользователей:");
                    sendMessageService.sendMessage(chatId, userService.getAllUsers());
                    panelSender.sendAdminPanel(chatId);
                } else {
                    sendMessageService.sendMessage(chatId, "Неизвестная команда: " + command);
                    panelSender.sendAdminPanel(chatId);
                }
                sendMessageService.sendMessage(chatId, "Неизвестный callbackData: " + callbackData);
                panelSender.sendAdminPanel(chatId);
            }
        }
    }

}
