package com.example.telegrambot.repository;

import com.example.telegrambot.model.Questions;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionsRepository extends JpaRepository<Questions, Long> {

    boolean existsByQuestion(String question);
    Questions getQuestionsById(Long id);
    @Query("SELECT q FROM Questions q")
    List<Questions> getAllQuestions();
    Optional<Questions> findFirstByActiveTrue();
    Questions getQuestionsByQuestion(String question);
    void deleteById(Long questionId);
}
