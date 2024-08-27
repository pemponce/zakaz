package com.example.telegrambot.repository;

import com.example.telegrambot.model.UserChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChatRepository extends JpaRepository<UserChat, Long> {
    boolean existsByChatId(Long chatId);
}