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
    @Query("SELECT MAX(id) AS max_id FROM Questions")
    Long getMaxId();
    @Query("SELECT MIN(id) AS min_id FROM Questions")
    Long getMinId();
    List<Questions> findByMorningTrueAndQuestionGroup(String group);
    Questions findFirstByMorningTrueAndQuestionGroupOrderByIdAsc(String group);
    Questions findFirstByMorningFalseAndQuestionGroupOrderByIdAsc(String group);
    List<Questions> findByMorningFalseAndQuestionGroup(String group);
    Questions getQuestionsByQuestion(String question);
}
