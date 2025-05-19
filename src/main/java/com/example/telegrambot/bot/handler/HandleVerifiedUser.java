package com.example.telegrambot.bot.handler;

import com.example.telegrambot.bot.panel.PanelSender;
import com.example.telegrambot.bot.state.StateService;
import com.example.telegrambot.bot.telegramSender.TelegramSender;
import com.example.telegrambot.help.Mailing;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.Role;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.AlertsService;
import com.example.telegrambot.service.QuestionsService;
import com.example.telegrambot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class HandleVerifiedUser {

    private final StateService stateService;
    private final HandleSetUserGroup handleSetUserGroup;
    private final PanelSender panelSender;
    private final SendMessageService messageService;
    private final HandleAdminCommands handleAdminCommands;
    private final QuestionsService questionsService;
    private final AlertsService alertsService;
    private final UserChatRepository userChatRepository;
    private final HandleUserRequest handleUserRequest;
    private final UserService userService;


    public void handleVerifiedUser(String text, Long chatId, Users currUser, Update update) {
        String userState = stateService.getState(chatId);

        if (currUser.getGroup() == null || currUser.getGroup().getName() == null) {
            if ("WAITING_FOR_GROUP".equals(stateService.getState(chatId))) {
                handleSetUserGroup.handleSetUserGroup(text, chatId, currUser);
            } else if (text.equals("Указать группу")) {
                stateService.setState(chatId, "WAITING_FOR_GROUP");
                messageService.sendMessage(chatId, "Введите номер своей группы в формате: 09-252");
            } else {
                panelSender.sendGroupPanel(chatId);
            }
        } else {
            if ("WAITING_FOR_NEW_QUESTION".equals(userState)) {

                if (text.equals("Отмена")) {
                    messageService.sendMessage(chatId, "Отмена действия");
                    stateService.clearState(chatId);
                    panelSender.sendAdminPanel(chatId);
                    return;
                }
                questionsService.createQuestion(text, userService.getUsersByChatId(chatId).getGroup().getName());
                messageService.sendMessage(chatId, "Новый вопрос добавлен.");
                stateService.clearState(chatId);
                panelSender.sendAdminPanel(chatId);
            } else if ("WAITING_FOR_NEW_BAN_QUESTION".equals(userState)) {
                if (text.equals("Отмена")) {
                    messageService.sendMessage(chatId, "Отмена действия");
                    stateService.clearState(chatId);
                    panelSender.sendAdminPanel(chatId);
                    return;
                }
                alertsService.createAlert(text, currUser.getGroup().getName());
                messageService.sendMessage(chatId, "Новое обьявление добавлено.");
                stateService.clearState(chatId);
                panelSender.sendAdminPanel(chatId);

            } else if ("WAITING_FOR_QUESTION_TO_DELETE".equals(userState)) {
                if (text.equals("Отмена")) {
                    messageService.sendMessage(chatId, "Отмена действия");
                    stateService.clearState(chatId);
                    panelSender.sendAdminPanel(chatId);
                    return;
                }
                questionsService.deleteQuestion(text);
                messageService.sendMessage(chatId, "Вопрос удалён.");
                stateService.clearState(chatId);
                panelSender.sendAdminPanel(chatId);
            } else if ("WAITING_FOR_BAN_QUESTION_TO_DELETE".equals(userState)) {
                if (text.equals("Отмена")) {
                    messageService.sendMessage(chatId, "Отмена действия");
                    stateService.clearState(chatId);
                    panelSender.sendAdminPanel(chatId);
                    return;
                }

                alertsService.deleteAlert(text, currUser.getGroup().getName());
                messageService.sendMessage(chatId, "Обьявление удалено.");
                stateService.clearState(chatId);
                panelSender.sendAdminPanel(chatId);

            } else {
                if (!Role.ADMIN.equals(currUser.getRole()) && text.equals("/admin")) {
                    messageService.sendMessage(chatId, "Вы не являетесь админом");
                } else {
                    UserChat user = userChatRepository.getUserChatByChatId(chatId);
                    List<Questions> questionsList = new ArrayList<>();
                    if (Mailing.wasLastMailingAQuestion()) {
                        questionsList = Mailing.isMorningLastMailing() ? questionsService.getMorningQuestions(currUser.getGroup().getName())
                                : questionsService.getNotMorningQuestions(currUser.getGroup().getName());
                    }

                    if (user.isWaitingForResponse()) {
                        handleUserRequest.userRequest(update, chatId, currUser, user, text, questionsList);
                    } else {
                        if (currUser.getRole().equals(Role.ADMIN)) {
                            handleAdminCommands.handleAdminCommands(text, chatId);
                        } else {
                            if (text.equals("Вывести уведомления")) {
                                var content = alertsService.getLastGroupAlert(currUser.getGroup().getName()).getContent();
                                messageService.sendMessage(chatId, content);
                            }
                            messageService.sendMessage(chatId, "Дождитесь 23:50 чтобы ответить на вопросы");
                            panelSender.sendUserPanel(chatId);
                        }
                    }
                }
            }
        }
    }

}
