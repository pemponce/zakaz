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
    List<Questions> findByMorningTrueAndRelevantTrueAndQuestionGroup(String group);
    List<Questions> findByMorningFalseAndRelevantTrueAndQuestionGroup(String group);
    Questions findFirstByMorningTrueAndRelevantTrueAndQuestionGroupOrderByIdAsc(String group);
    Questions findFirstByMorningFalseAndRelevantTrueAndQuestionGroupOrderByIdAsc(String group);
    List<Questions> findAllByRelevantTrue();
    List<Questions> findAllByRelevantTrueAndQuestionGroup(String group);
    Questions getQuestionsByQuestion(String question);
}
