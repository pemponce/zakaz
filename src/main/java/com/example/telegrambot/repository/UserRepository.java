package com.example.telegrambot.repository;

import com.example.telegrambot.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByChatId(Long id);
    Users getUsersByUsername(String username);
}
