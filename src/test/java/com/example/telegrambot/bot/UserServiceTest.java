package com.example.telegrambot.bot;

import com.example.telegrambot.model.Group;
import com.example.telegrambot.model.Role;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.GroupRepository;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserChatRepository userChatRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MyTelegramBot myTelegramBot;

    private static final Path INDEX_FILE = Paths.get("last_index.txt");

    private int loadLastIndex() {
        try {
            if (Files.exists(INDEX_FILE)) {
                String content = Files.readString(INDEX_FILE);
                return Integer.parseInt(content.trim());
            } else {
                return 0;
            }
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Failed to read last index", e);
        }
    }

    private void saveLastIndex(int index) {
        try {
            Files.writeString(INDEX_FILE, String.valueOf(index));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save last index", e);
        }
    }


    @Test
    void createUser() {
        int i = loadLastIndex();
        int maxlength = i + 100;
        for (int c = i + 1; c <= maxlength; c++) {
            Update update1 = new Update();
            var user = createUser(c);
            update1.setMessage(message(c, user));
            myTelegramBot.onUpdateReceived(update1);

            if (c == maxlength) {
                saveLastIndex(c);
            }
        }
    }

    private Users createUser(int id) {
        Users user = new Users();
        UserChat userChat = new UserChat();

        Group group = new Group();
        if (groupRepository.getByName("09-252") == null) {

            group.setName("09-252");
            group.setSpreadsheetId("13gcdpD2HtassBH1GlhnZhUhPVS_iIES-Umk0MiEIuLA");
            groupRepository.save(group);
        } else {
            group = groupRepository.getByName("09-252");
        }

        user.setUsername("username" + id);
        user.setVerify(true);
        user.setGroup(group);
        user.setRole(Role.USER);
        user.setChatId(677132443 + ((long) id));
        userRepository.save(user);

        userChat.setWaitingForResponse(false);
        userChat.setCurrentQuestionId(null);
        userChat.setChatId(677132443 + ((long) id));
        userChatRepository.save(userChat);

        return user;
    }

    private Message message(int id, Users user) {
        Message message = new Message();
        User user1 = new User();
        user1.setUserName(user.getUsername());
        Chat chat = new Chat();
        chat.setId(677132443L + (long) id);
        chat.setFirstName("username" + id);
        message.setText("gello" + id);
        message.setFrom(user1);
        message.setChat(chat);
        return message;
    }
}
