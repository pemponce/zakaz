package com.example.telegrambot.service.impl;

import com.example.telegrambot.help.GenerateCode;
import com.example.telegrambot.model.Message;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.model.UserChat;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.model.enumRole.Role;
import com.example.telegrambot.repository.UserChatRepository;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserChatRepository userChatRepository;
    private UserRepository userRepository;
    private final GenerateCode generateCode;


    @Override
    public Users createUser(Update update) {
        Long chatId = update.getMessage().getChatId();

        UserChat userChat = UserChat.builder().chatId(chatId).build();
        userChat.setChatId(chatId);
        userChatRepository.save(userChat);

        int verificationCode = generateCode.generateCode();


        Users user = Users.builder()
                .username(update.getMessage().getFrom().getUserName())
                .verificationCode(verificationCode)
                .role(Role.USER)
                .chatId(chatId)
                .build();

        return userRepository.save(user);
    }



    @Override
    public String getAllUsers() {
        String res = "";
        List<Users> users = new ArrayList<>(userRepository.findAll());
        int counter = 1;
        for (Users user : users) {
            res += counter + " - " + user.getUsername() + "\n";
            counter++;
        }
        return res;
    }

    @Override
    public boolean setRole(String username, String role) {
        boolean flag = true;

        if(userRepository.getUsersByUsername(username) != null) {
            Users user = userRepository.getUsersByUsername(username);

            switch (role) {
                case "admin" -> {
                    user.setRole(Role.ADMIN);
                    userRepository.save(user);
                }
                case "user" -> {
                    user.setRole(Role.USER);
                    userRepository.save(user);
                }
                default -> flag = false;
            }

        } else {
            flag = false;
        }

        return flag;
    }

    @Override
    public boolean existsByVerificationCode(int code, Users currUser) {
        return userRepository.existsByVerificationCode(code) && currUser.getVerificationCode() == code ;
    }

    @Override
    public void verifyUser(int code, Users currUser) {
        currUser.setVerify(true);
        userRepository.save(currUser);
    }

}
