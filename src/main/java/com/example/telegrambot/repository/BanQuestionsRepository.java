package com.example.telegrambot.repository;

import com.example.telegrambot.model.BanQuestions;
import com.example.telegrambot.model.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BanQuestionsRepository extends JpaRepository<BanQuestions, Long> {
    boolean existsByQuestion(String question);
    BanQuestions getBanQuestionsById(Long id);
    @Query("SELECT q FROM BanQuestions q")
    List<BanQuestions> getAllBanQuestions();
    @Query("SELECT MAX(id) AS max_id FROM BanQuestions")
    Long getMaxId();
    @Query("SELECT MIN(id) AS min_id FROM BanQuestions")
    Long getMinId();
    BanQuestions getBanQuestionsByQuestion(String question);
    void deleteById(Long questionId);
}
