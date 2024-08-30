package com.example.telegrambot.repository;

import com.example.telegrambot.model.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionsRepository extends JpaRepository<Questions, Long> {

    Questions getQuestionsById(Long id);

    @Query("SELECT q FROM Questions q")
    List<String> getAllQuestions();

}