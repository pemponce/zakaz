//package com.example.telegrambot.help;
//
//import com.example.telegrambot.executors.Executor;
//import com.example.telegrambot.model.*;
//import com.example.telegrambot.repository.UserChatRepository;
//import com.example.telegrambot.service.AlertsService;
//import com.example.telegrambot.service.UserService;
//import com.example.telegrambot.service.impl.QuestionsServiceImpl;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@Slf4j
//@AllArgsConstructor
//public class Mailing {
//
//    private Executor executor;
//    @Autowired
//    private UserChatRepository userChatRepository;
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private QuestionsServiceImpl questionsService;
//
//    @Autowired
//    private AlertsService alertsService;
//
//    private static boolean status;
//    private static boolean alertStatus;
//
//    @Scheduled(cron = "0 0/1 * * * *")
//    public void sendDailyMessage() {
//        status = false;
//        List<UserChat> chatUsers = userChatRepository.findAll();
//
//        for (UserChat chatUser : chatUsers) {
//            Long chatId = chatUser.getChatId();
//            Users user = userService.getUsersByChatId(chatId);
//            if (checkValidUserAndTypeOfQuestionOrAlertMailing(user)) {
//                executor.broadcastMessage(chatId, "Пожалуйста ответьте на все вопросы");
//
//                Questions currentQuestion = questionsService.findFirstByMorningFalse(user.getGroup().getName());
//                chatUser.setCurrentQuestionId(currentQuestion.getId());
//                chatUser.setWaitingForResponse(true);
//                userChatRepository.save(chatUser);
//
//                executor.broadcastMessage(chatId, currentQuestion.getQuestion());
//
//            } else {
//                execute(user);
//            }
//        }
//    }
//
//    @Scheduled(cron = "0 30 10 * * *")
//    public void sendMorningQuestions() {
//        status = true;
//        List<UserChat> chatUsers = userChatRepository.findAll();
//
//        for (UserChat chatUser : chatUsers) {
//            Long chatId = chatUser.getChatId();
//            Users user = userService.getUsersByChatId(chatId);
//
//            if (checkValidUserAndTypeOfQuestionOrAlertMailing(user)) {
//                executor.broadcastMessage(chatId, Emoji.QUESTION.getData() + "Пожалуйста ответьте на все вопросы");
//
//                Questions currentQuestion = questionsService.findFirstByMorningTrue(user.getGroup().getName());
//                chatUser.setCurrentQuestionId(currentQuestion.getId());
//                chatUser.setWaitingForResponse(true);
//                userChatRepository.save(chatUser);
//
//                executor.broadcastMessage(chatId, currentQuestion.getQuestion());
//
//            } else {
//                execute(user);
//            }
//        }
//    }
//
//
////    @Scheduled(cron = "0 30 17 * * *")
//    @Scheduled(cron = "0 0/1 * * * *")
//    public void sendAlert() {
//        alertStatus = true;
//        List<UserChat> chatUsers = userChatRepository.findAll();
//
//        for (UserChat chatUser : chatUsers) {
//            Long chatId = chatUser.getChatId();
//            Users user = userService.getUsersByChatId(chatId);
//
//            if (checkValidUserAndTypeOfQuestionOrAlertMailing(user)) {
//                executor.broadcastMessage(chatId, Emoji.ALERT.getData() + " Оповещение для группы " + user.getGroup().getName() + ":\n" + alertsService.getLastGroupAlert(user.getGroup().getName()));
//                alertStatus = false;
//            }
//        }
//    }
//
//    public boolean checkValidUserAndTypeOfQuestionOrAlertMailing(Users user) {
//        return alertStatus ? !user.getRole().equals(Role.ADMIN) && user.isVerify() && alertsService.getLastGroupAlert(user.getGroup().getName()) != null : status ? !user.getRole().equals(Role.ADMIN) && user.isVerify() && questionsService.findFirstByMorningTrue(user.getGroup().getName()) != null : !user.getRole().equals(Role.ADMIN) && user.isVerify() && questionsService.findFirstByMorningFalse(user.getGroup().getName()) != null;
//    }
//
//    public void execute(Users user) {
//        if (!user.isVerify()) {
//            if (status) {
//                executor.broadcastMessage(user.getChatId(), "Авторизируйтесь! следующая рассылка будет в 20:30");
//            } else {
//                executor.broadcastMessage(user.getChatId(), "Авторизируйтесь! следующая рассылка будет в 10:30");
//            }
//        }
//        if (user.isVerify() && user.getRole().equals(Role.USER) && (questionsService.findFirstByMorningTrue(user.getGroup().getName()) == null) || (questionsService.findFirstByMorningFalse(user.getGroup().getName()) == null)) {
//            if (status) {
//                executor.broadcastMessage(user.getChatId(), Emoji.WARNING + " Утренних вопросов сегодня нет");
//            } else {
//                executor.broadcastMessage(user.getChatId(), Emoji.WARNING + " Дневных вопросов сегодня нет");
//            }
//        } else {
//            executor.broadcastMessage(user.getChatId(), Emoji.WARNING + " Рассылка началась" + Emoji.ALERT);
//        }
//    }
//
//    public static boolean morningQuestion() {
//        return status;
//    }
//}

package com.example.telegrambot.help;

import com.example.telegrambot.executors.Executor;
import com.example.telegrambot.model.*;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.service.AlertsService;
import com.example.telegrambot.service.UserService;
import com.example.telegrambot.service.impl.QuestionsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class Mailing {

    private final Executor executor;
    private final UserChatRepository userChatRepository;
    private final UserService userService;
    private final QuestionsServiceImpl questionsService;
    private final AlertsService alertsService;

    private static MailingType lastMailingType;

    private enum MailingType {
        MORNING, DAILY, ALERT
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendDaily() {
        sendToAllUsers(MailingType.DAILY);
    }

    @Scheduled(cron = "0 30 10 * * *")
    public void sendMorning() {
        sendToAllUsers(MailingType.MORNING);
    }

    @Scheduled(cron = "0 30 2 * * *")
    public void sendAlerts() {
        sendToAllUsers(MailingType.ALERT);
    }

    private void sendToAllUsers(MailingType type) {
        List<UserChat> users = userChatRepository.findAll();

        for (UserChat chat : users) {
            var user = userService.getUsersByChatId(chat.getChatId());

            if (isUserEligible(user)) {
                processUserByType(user, chat, type);
            } else {
                handleIneligibleUser(user, type);
            }
        }
    }

    private boolean isUserEligible(Users user) {

        return user.isVerify() && user.getRole() != Role.ADMIN;
    }

    private void processUserByType(Users user, UserChat chat, MailingType type) {

        switch (type) {
            case MORNING -> {
                sendQuestion(user, chat, true);
            }
            case DAILY -> {
                sendQuestion(user, chat, false);
            }
            case ALERT -> {
                sendAlert(user, chat);
            }
        }
    }

    private void sendAlert(Users user, UserChat chat) {
        var alertText = alertsService.getLastGroupAlert(user.getGroup().getName()).getContent();
        if (alertText != null) {

            alertText = highlightEnglishWordsAsCode(alertText);
            executor.broadcastMessage(chat.getChatId(), Emoji.ALERT.getData().repeat(3) + "\nОповещение для группы " +
                    user.getGroup().getName() + ":\n" + "<strong>" + alertText + "</strong>");
        } else {
            executor.broadcastMessage(chat.getChatId(), Emoji.ALERT.getData().repeat(3) + "\nНет оповещений для группы " +
                    user.getGroup().getName());
        }
    }

    private void sendQuestion(Users user, UserChat chat, boolean morning) {
        var question = morning
                ? questionsService.findFirstByMorningTrue(user.getGroup().getName())
                : questionsService.findFirstByMorningFalse(user.getGroup().getName());

        if (question != null) {
            executor.broadcastMessage(chat.getChatId(), Emoji.QUESTION.getData().repeat(3) + "\nПожалуйста ответьте на все вопросы");

            chat.setCurrentQuestionId(question.getId());
            chat.setWaitingForResponse(true);
            userChatRepository.save(chat);
            executor.broadcastMessage(user.getChatId(), question.getQuestion());
        } else {
            executor.broadcastMessage(chat.getChatId(), Emoji.QUESTION.getData().repeat(3) + "\nВопросов сегодня нет" + Emoji.ALERT.getData());
        }
        lastMailingType = morning ? MailingType.MORNING : MailingType.DAILY;
    }

    private void handleIneligibleUser(Users user, MailingType type) {
        if (!user.isVerify()) {
            var timeHint = type == MailingType.MORNING ? "10:30" : "20:30";
            executor.broadcastMessage(user.getChatId(), "Авторизируйтесь! следующая рассылка будет в " + timeHint);
            return;
        }

        var message = switch (type) {
            case MORNING -> Emoji.WARNING + " Рассылка утренних вопросов началась";
            case DAILY -> Emoji.WARNING + " Рассылка дневных вопросов началась";
            case ALERT -> Emoji.WARNING + " Рассылка актуальных оповещений началась";
        };

        executor.broadcastMessage(user.getChatId(), message);
    }

    public String highlightEnglishWordsAsCode(String text) {
        return text.replaceAll("(?<!\\p{L})([a-zA-Z@#0-9_.\\-]{2,})(?!\\p{L})", "<code>$1</code>");
    }


    public static MailingType getLastMailingType() {
        return lastMailingType;
    }

    public static boolean isMorningLastMailing() {
        return lastMailingType == MailingType.MORNING;
    }

    public static boolean isDailyLastMailing() {
        return lastMailingType == MailingType.DAILY;
    }

    public static boolean wasLastMailingAQuestion() {
        return lastMailingType == MailingType.MORNING || lastMailingType == MailingType.DAILY;
    }
}
