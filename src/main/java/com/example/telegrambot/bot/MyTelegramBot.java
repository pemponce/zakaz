package com.example.telegrambot.bot;

import com.example.telegrambot.command.AdminPanel;
import com.example.telegrambot.command.AuthPanel;
import com.example.telegrambot.command.UserPanel;
import com.example.telegrambot.googleSheets.service.GoogleSheetsService;
import com.example.telegrambot.help.Mailing;
import com.example.telegrambot.model.BanQuestions;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.model.enumRole.Role;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.BanQuestionsService;
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

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botName;

    @Autowired
    private UserChatRepository userChatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private QuestionsService questionsService;

    @Autowired
    private BanQuestionsService banQuestionsService;

    @Autowired
    private MessageService messageService;

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
                Users user = userService.createUser(update);

                try {
                    googleSheetsService.createList(user.getUsername());
                    sendWelcomeMessage(chatId);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(chatId, "Ошибка при создании листа в Google Sheets.");
                }
            }
        } else {
            if (currUser.isVerify()) {
                handleVerifiedUser(text, chatId, currUser, update);
            } else {
                handleNonVerifiedUser(text, chatId, currUser);
            }
        }
    }

    private void handleVerifiedUser(String text, Long chatId, Users currUser, Update update) {
        String userState = userStates.getOrDefault(chatId, "");


        if ("WAITING_FOR_NEW_QUESTION".equals(userState)) {

            if (text.equals("Отмена")) {
                sendMessage(chatId, "Отмена действия");
                userStates.remove(chatId);
                sendAdminPanel(chatId);
                return;
            }
            Questions newQuestion = new Questions();
            newQuestion.setQuestion(text);
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
            BanQuestions newBanQuestion = new BanQuestions();
            newBanQuestion.setQuestion(text);
            banQuestionsService.createQuestion(newBanQuestion.getQuestion());
            sendMessage(chatId, "Новый вопрос для бана добавлен.");
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
            banQuestionsService.deleteQuestion(text);
            sendMessage(chatId, "Вопрос для бана удалён.");
            userStates.remove(chatId);
            sendAdminPanel(chatId);
        } else {
            if (!Role.ADMIN.equals(currUser.getRole()) && text.equals("/admin")) {
                sendMessage(chatId, "Вы не являетесь админом");
            } else {
                UserChat user = userChatRepository.getUserChatByChatId(chatId);
                List<Questions> questionsList = Mailing.morningQuestion() ?
                        new ArrayList<>(questionsService.getMorningQuestions()) :
                        new ArrayList<>(questionsService.getNotMorningQuestions());

                if (user.isWaitingForResponse()) {
                    if (!ban) {
                        userRequest(update, chatId, currUser, user, text, questionsList, null);
                    } else {
                        List<BanQuestions> banQuestionsList = new ArrayList<>(banQuestionsService.getAll());
                        userRequest(update, chatId, currUser, user, text, null, banQuestionsList);

                    }
                } else {
                    if (currUser.getRole().equals(Role.ADMIN)) {
                        handleAdminCommands(text, chatId);
                    } else {
                        if (text.equals("Забанили")) {
                            long minId = banQuestionsService.getMinId();
                            user.setCurrentQuestionId(minId);
                            user.setWaitingForResponse(true);
                            userChatRepository.save(user);
                            sendMessage(chatId, banQuestionsService.getQuestion(minId).getQuestion());
                            ban = true;
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
                             String text, @Nullable List<Questions> questionsList, @Nullable List<BanQuestions> banQuestionsList) {

        long currentQuestionId = user.getCurrentQuestionId();
        long maxId;
        long minId;
        boolean flag = false;

        Object nextQuestion;
        if (questionsList != null) {
            maxId = questionsService.getMaxId();
            minId = questionsService.getMinId();

            nextQuestion = questionsList.stream()
                    .filter(q -> q.getId() > currentQuestionId && q.getId() <= maxId)
                    .findFirst()
                    .orElse(null);
            sendMessage(chatId, "Ваш ответ записан\n```\n" + text + "\n```");

            messageService.saveMessage(update, null, currUser, flag);

        } else {


            maxId = banQuestionsService.getMaxId();
            minId = banQuestionsService.getMinId();

            nextQuestion = banQuestionsList.stream()
                    .filter(q -> q.getId() > currentQuestionId && q.getId() <= maxId)
                    .findFirst()
                    .orElse(null);

            flag = true;
            sendMessage(chatId, "Ваш ответ записан\n```\n" + text + "\n```");

            answers.add(text);

        }


        if (nextQuestion != null) {
            if (nextQuestion instanceof Questions) {
                user.setCurrentQuestionId(((Questions) nextQuestion).getId());
                sendMessage(chatId, ((Questions) nextQuestion).getQuestion());
            } else {
                user.setCurrentQuestionId(((BanQuestions) nextQuestion).getId());
                sendMessage(chatId, ((BanQuestions) nextQuestion).getQuestion());
            }

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
            case "Добавить вопрос" -> sendQuestionTypeButtons(chatId, "add");
            case "Удалить вопрос" -> sendQuestionTypeButtons(chatId, "delete");
            case "Вывести все вопросы" -> sendQuestionTypeButtons(chatId, "list");
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
                } else {
                    sendMessage(chatId, "Код неверный, пожалуйста запросите код у администратора");
                    sendAuthPanel(chatId);
                }
                break;
            default:
                if (text.equals("Авторизоваться")) {
                    sendMessage(chatId, "Введите 4х значный код");
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
                sendMessage(chatId, "Пожалуйста, введите новый обычный вопрос:");
            }
            case "add_ban" -> {
                sendCancelBtn(chatId);
                userStates.put(chatId, "WAITING_FOR_NEW_BAN_QUESTION");
                sendMessage(chatId, "Пожалуйста, введите новый вопрос для бана:");
            }
            case "delete_normal" -> {
                sendCancelBtn(chatId);
                userStates.put(chatId, "WAITING_FOR_QUESTION_TO_DELETE");
                sendMessage(chatId, "Введите обычный вопрос, который хотите удалить:");
            }
            case "delete_ban" -> {
                sendCancelBtn(chatId);
                userStates.put(chatId, "WAITING_FOR_BAN_QUESTION_TO_DELETE");
                sendMessage(chatId, "Введите вопрос для бана, который хотите удалить:");
            }
            case "list_normal" -> {
                sendMessage(chatId, "Вот список всех обычных вопросов:");
                sendMessage(chatId, questionsService.getAllQuestions());
                sendAdminPanel(chatId);
            }
            case "list_ban" -> {
                sendMessage(chatId, "Вот список всех вопросов для бана:");
                sendMessage(chatId, banQuestionsService.getAllQuestions());
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
        message.setText("Если у вас произошел бан, тыкните на кнопку ниже");
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
            case "add" -> "Выберите тип вопроса для добавления:";
            case "delete" -> "Выберите тип вопроса для удаления:";
            case "list" -> "Выберите тип вопроса для отображения:";
            default -> "Ошибка!";
        };
        message.setText(text);
        message.setReplyMarkup(AdminPanel.questionTypeButtons(action));

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
