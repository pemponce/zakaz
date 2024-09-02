package com.example.telegrambot.service;

import com.example.telegrambot.model.Questions;

import java.util.List;
import java.util.Optional;

public interface QuestionsService {
    Long getQuestionsLength();
    Questions getQuestion(Long id);
    boolean createQuestion(String question);
    void deleteQuestion(String question);
    String getAllQuestions();
    List<Questions> getAll();
    Long getMaxId();
    Long getMinId();
    Optional<Questions> findActiveQuestion();
    void saveQuestion(Questions question);
}
