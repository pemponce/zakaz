package com.example.telegrambot.repository;

import com.example.telegrambot.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotMessageRepository extends JpaRepository<Message, Long> {
    Message getTopByChatId(Long chatId);
}
