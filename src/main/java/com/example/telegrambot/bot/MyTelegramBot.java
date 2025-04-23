package com.example.telegrambot.bot;

import com.example.telegrambot.command.AdminPanel;
import com.example.telegrambot.command.AuthPanel;
import com.example.telegrambot.command.GroupPanel;
import com.example.telegrambot.command.UserPanel;
import com.example.telegrambot.googleSheets.service.GoogleSheetsService;
import com.example.telegrambot.help.Mailing;
import com.example.telegrambot.model.*;
import com.example.telegrambot.model.Role;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.AlertsService;
import com.example.telegrambot.service.GroupService;
import com.example.telegrambot.service.MessageService;
import com.example.telegrambot.service.QuestionsService;
import com.example.telegrambot.service.impl.UserServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botName;

    @Autowired
    private UserChatRepository userChatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertsService alertsService;

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private QuestionsService questionsService;

    @Autowired
    private MessageService messageService;
    @Autowired
    private GroupService groupService;

    private final Map<Long, String> userStates = new HashMap<>();

    @Autowired
    private GoogleSheetsService googleSheetsService;

    private boolean ban;
    private final ArrayList<Object> answers = new ArrayList<>();

    public MyTelegramBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallback(update);
        } else {
            System.out.println("Неизвестный тип обновления");
        }
    }

    private void handleMessage(Update update) {
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
                handleVerifiedUser(text, chatId, currUser, update);

            } else {
                handleNonVerifiedUser(text, chatId, currUser);
            }
        }
    }

    private void handleSetUserGroup(String text, Long chatId, Users currUser) {
        try {
            String groupSpreadsheetId = googleSheetsService.createSpreadsheetForGroup(text);


            if (groupService.findByName(text).isEmpty()) {
                Group group = Group.builder()
                        .name(text)
                        .spreadsheetId(groupSpreadsheetId)
                        .build();

                groupService.create(group);
                userService.updateUserGroup(currUser.getUsername(), group);
                googleSheetsService.createList(currUser.getUsername(), groupSpreadsheetId);
                sendMessage(chatId, "Создана таблица для группы " + text + "\nДобавлен лист для " + currUser.getUsername());
            } else {
                userService.updateUserGroup(currUser.getUsername(), groupService.getByName(text));
                googleSheetsService.createList(currUser.getUsername(), groupSpreadsheetId);
                sendMessage(chatId, "Добавлен лист для " + currUser.getUsername());
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(chatId, "Ошибка при создании таблицы в Google Sheets.");
        }
        userStates.remove(chatId);
    }

    private void handleVerifiedUser(String text, Long chatId, Users currUser, Update update) {
        String userState = userStates.getOrDefault(chatId, "");

        if (currUser.getGroup() == null || currUser.getGroup().getName() == null) {
            if ("WAITING_FOR_GROUP".equals(userStates.get(chatId))) {
                handleSetUserGroup(text, chatId, currUser);
            } else if (text.equals("Указать группу")) {
                userStates.put(chatId, "WAITING_FOR_GROUP");
                sendMessage(chatId, "Введите номер своей группы в формате: 09-252");
            } else {
                sendGroupPanel(chatId);
            }
            return;
        } else {
            if ("WAITING_FOR_NEW_QUESTION".equals(userState)) {

                if (text.equals("Отмена")) {
                    sendMessage(chatId, "Отмена действия");
                    userStates.remove(chatId);
                    sendAdminPanel(chatId);
                    return;
                }
                Questions newQuestion = new Questions();
                newQuestion.setQuestion(text);
                newQuestion.setQuestionGroup(currUser.getGroup().getName());
                questionsService.saveQuestion(newQuestion);
                sendMessage(chatId, "Новый вопрос добавлен.");
                userStates.remove(chatId);
                sendAdminPanel(chatId);
            } else if ("WAITING_FOR_NEW_BAN_QUESTION".equals(userState)) {
                if (text.equals("Отмена")) {
                    sendMessage(chatId, "Отмена действия");
                    userStates.remove(chatId);
                    sendAdminPanel(chatId);
                    return;
                }
                alertsService.createAlert(text, currUser.getGroup().getName());
                sendMessage(chatId, "Новое обьявление добавлено.");
                userStates.remove(chatId);
                sendAdminPanel(chatId);

            } else if ("WAITING_FOR_QUESTION_TO_DELETE".equals(userState)) {
                if (text.equals("Отмена")) {
                    sendMessage(chatId, "Отмена действия");
                    userStates.remove(chatId);
                    sendAdminPanel(chatId);
                    return;
                }
                questionsService.deleteQuestion(text);
                sendMessage(chatId, "Вопрос удалён.");
                userStates.remove(chatId);
                sendAdminPanel(chatId);
            } else if ("WAITING_FOR_BAN_QUESTION_TO_DELETE".equals(userState)) {
                if (text.equals("Отмена")) {
                    sendMessage(chatId, "Отмена действия");
                    userStates.remove(chatId);
                    sendAdminPanel(chatId);
                    return;
                }

                alertsService.deleteAlert(text, currUser.getGroup().getName());
                sendMessage(chatId, "Обьявление удалено.");
                userStates.remove(chatId);
                sendAdminPanel(chatId);

            } else {
                if (!Role.ADMIN.equals(currUser.getRole()) && text.equals("/admin")) {
                    sendMessage(chatId, "Вы не являетесь админом");
                } else {
                    UserChat user = userChatRepository.getUserChatByChatId(chatId);
                    List<Questions> questionsList = new ArrayList<>();
                    if (Mailing.wasLastMailingAQuestion()) {
                        questionsList = Mailing.isMorningLastMailing()
                                ? questionsService.getMorningQuestions(currUser.getGroup().getName())
                                : questionsService.getNotMorningQuestions(currUser.getGroup().getName());
                    }

                    if (user.isWaitingForResponse()) {
                        userRequest(update, chatId, currUser, user, text, questionsList);
                    } else {
                        if (currUser.getRole().equals(Role.ADMIN)) {
                            handleAdminCommands(text, chatId);
                        } else {
                            sendMessage(chatId, "Дождитесь 23:50 чтобы ответить на вопросы");
                            sendUserPanel(chatId);
                        }
                    }
                }
            }
        }
    }

    private void userRequest(Update update, Long chatId, Users currUser, UserChat user,
                             String text, @Nullable List<Questions> questionsList) {

        long currentQuestionId = user.getCurrentQuestionId();
        long maxId;
        long minId;
        boolean flag = false;

        Questions nextQuestion;
        maxId = questionsService.getMaxId();
        minId = questionsService.getMinId();

        nextQuestion = questionsList.stream()
                .filter(q -> q.getId() > currentQuestionId && q.getId() <= maxId)
                .findFirst()
                .orElse(null);
        sendMessage(chatId, "Ваш ответ записан\n<strong>" + text + "</strong>");

        messageService.saveMessage(update, null, currUser, flag);


        if (nextQuestion != null) {
            user.setCurrentQuestionId(nextQuestion.getId());
            sendMessage(chatId, nextQuestion.getQuestion());

            user.setWaitingForResponse(true);
            userChatRepository.save(user);
        }
        // Если вопросов больше нет
        else {
            user.setWaitingForResponse(false);
            user.setCurrentQuestionId(minId); // Сбрасываем на минимальный ID
            userChatRepository.save(user);
            if (ban) {
                messageService.saveMessage(update, answers, currUser, flag);
                answers.clear();
            }
            ban = false;
            sendMessage(chatId, "Спасибо! Вы ответили на все вопросы.");
        }
    }

    private void handleAdminCommands(String text, Long chatId) {
        String[] parts = text.split(" ");

        switch (parts[0]) {
            case "/admin" -> sendAdminPanel(chatId);
            case "/setrole" -> handleSetRoleCommand(text, chatId);
            case "/help" -> sendHelpMessage(chatId);
            case "/setTime" -> handleSetIsMorning(text, chatId);
            default -> sendQuestionTypeButtonsBasedOnText(text, chatId);
        }
    }

    private void handleSetRoleCommand(String text, Long chatId) {
        String[] parts = text.split(" ");
        if (parts.length == 3) {
            String username = parts[1];
            String role = parts[2].toLowerCase();

            if (userService.setRole(username, role)) {
                sendMessage(chatId, "Пользователю " + username + " выдана роль " + role);
            } else {
                sendMessage(chatId, "Ошибка! Такого пользователя или роли не существует!");
            }
        } else {
            sendMessage(chatId, "Неправильный формат команды. Используйте: /setrole username role");
        }
        sendAdminPanel(chatId);
    }

    private void handleSetIsMorning(String text, Long chatId) {
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
            sendMessage(chatId, "Вопросу " + question + " выдана роль " + isMorning);
        } else {
            sendMessage(chatId, "Ошибка! Такого вопроса или параметра не существует!");
        }

        sendAdminPanel(chatId);
    }

    private void sendHelpMessage(Long chatId) {
        sendMessage(chatId, """
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

    private void sendQuestionTypeButtonsBasedOnText(String text, Long chatId) {
        switch (text) {
            case "Добавить данные" -> sendQuestionTypeButtons(chatId, "add");
            case "Удалить данные" -> sendQuestionTypeButtons(chatId, "delete");
            case "Вывести все данные" -> sendQuestionTypeButtons(chatId, "list");
            case "Вывести всех пользователей" -> {
                sendMessage(chatId, "Вот список всех пользователей:");
                sendMessage(chatId, userService.getAllUsers());
            }
            default -> sendMessage(chatId, "Неизвестная команда. Пожалуйста, используйте доступные команды.");
        }
    }

    private void handleNonVerifiedUser(String text, Long chatId, Users currUser) {
        String userState = userStates.getOrDefault(chatId, "");

        switch (userState) {
            case "AuthUser":
                if (userService.existsByVerificationCode(Integer.parseInt(text), currUser)) {
                    userService.verifyUser(Integer.parseInt(text), currUser);
                    sendMessage(chatId, "Вы успешно авторизовались");
                    sendGroupPanel(chatId);
                } else {
                    sendMessage(chatId, "Код неверный, пожалуйста запросите код у администратора");
                    sendAuthPanel(chatId);
                }
                break;
            default:
                if (text.equals("Авторизоваться")) {
                    sendMessage(chatId, "Введите 4-значный код");
                    userStates.put(chatId, "AuthUser");
                } else {
                    sendAuthPanel(chatId);
                }
                break;
        }
    }

    private void handleCallback(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        String command = update.getCallbackQuery().getMessage().toString();

        switch (callbackData) {
            case "add_normal" -> {
                sendCancelBtn(chatId);
                userStates.put(chatId, "WAITING_FOR_NEW_QUESTION");
                sendMessage(chatId, "Пожалуйста, введите новый вопрос:");
            }
            case "add_info" -> {
                sendCancelBtn(chatId);
                userStates.put(chatId, "WAITING_FOR_NEW_BAN_QUESTION");
                sendMessage(chatId, "Пожалуйста, введите новое обьявление/оповещение:");
            }
            case "delete_normal" -> {
                sendCancelBtn(chatId);
                userStates.put(chatId, "WAITING_FOR_QUESTION_TO_DELETE");
                sendMessage(chatId, "Введите обычный вопрос, который хотите удалить:");
            }
            case "delete_info" -> {
                sendCancelBtn(chatId);
                userStates.put(chatId, "WAITING_FOR_BAN_QUESTION_TO_DELETE");
                sendMessage(chatId, "Введите обьявление/оповещение, которое хотите удалить:");
            }
            case "list_normal" -> {
                sendMessage(chatId, "Вот список всех вопросов:");
                sendMessage(chatId, questionsService.getAllQuestionsContent());
                sendAdminPanel(chatId);
            }
            case "list_info" -> {
                sendMessage(chatId, "Вот список всех оповещений:");

                /*
                TODO: вывести это логику в сервайс алертс
                 */

                List<Alerts> alerts = alertsService.getAllAlerts(userRepository.getUsersByChatId(chatId).getGroup().getName());

                if (alerts.size() > 0) {
                    sendMessage(chatId, "Оповещения для группы - " + userRepository.getUsersByChatId(chatId).getGroup().getName());

                    String messageText = IntStream.range(0, alerts.size())
                            .mapToObj(i -> (alerts.get(i).isActive() ?
                                    Emoji.ALARM.getData() + " " : Emoji.CHECKED.getData() + " ")
                                    + (i + 1) + " - " + alerts.get(i).getContent()
                            )
                            .collect(Collectors.joining("\n"));


                    sendMessage(chatId, messageText);

                } else {
                    sendMessage(chatId, "нет оповощений для группы - " + userRepository.getUsersByChatId(chatId).getGroup().getName());
                }

                sendAdminPanel(chatId);
            }
            default -> {
                if ("list_users".equals(command)) {
                    sendMessage(chatId, "Вот список всех пользователей:");
                    sendMessage(chatId, userService.getAllUsers());
                    sendAdminPanel(chatId);
                } else {
                    sendMessage(chatId, "Неизвестная команда: " + command);
                    sendAdminPanel(chatId);
                }
                sendMessage(chatId, "Неизвестный callbackData: " + callbackData);
                sendAdminPanel(chatId);
            }
        }
    }

    private void sendAdminPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Выберите действие:");
        message.setReplyMarkup(AdminPanel.adminActions());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendUserPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Если хотите увидеть истотрию уведомлений нажмите на кнопку");
        message.setReplyMarkup(UserPanel.userActions());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendQuestionTypeButtons(Long chatId, String action) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        String text = switch (action) {
            case "add" -> "Выберите тип данных для добавления:";
            case "delete" -> "Выберите тип данных для удаления:";
            case "list" -> "Выберите тип данных для отображения:";
            default -> "Ошибка!";
        };
        message.setText(text);
        message.setReplyMarkup(AdminPanel.questionAndAlertTypeButtons(action));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAuthPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Вы не авторизованы. Пожалуйста, авторизуйтесь:");
        message.setReplyMarkup(AuthPanel.authAction());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendGroupPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Вы не указали свою группу, пожалуйста укажите ее");
        message.setReplyMarkup(GroupPanel.groupAction());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = "Привет! Добро пожаловать в нашего бота. Если вы являетесь админом то напишите /admin," + " после можете ознакомится с командами бота (/help)";
        sendMessage(chatId, welcomeText);
        sendAuthPanel(chatId);
    }

    private void sendCancelBtn(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Если хотите отменить, нажмите кнопку ниже:");
        message.setReplyMarkup(AdminPanel.adminCancelBtn());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setParseMode("HTML");
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
