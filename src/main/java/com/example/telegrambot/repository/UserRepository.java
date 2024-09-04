package com.example.telegrambot.repository;

import com.example.telegrambot.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByChatId(Long id);
    Users getUsersByUsername(String username);
    Users getUsersByChatId(Long chatId);
    boolean existsByVerificationCode(int code);

}
