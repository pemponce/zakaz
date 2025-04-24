package com.example.telegrambot.service;

import com.example.telegrambot.model.Questions;

import java.util.List;
import java.util.Optional;

public interface QuestionsService {
    Long getQuestionsLength();
    Questions getQuestion(Long id);
    boolean createQuestion(String question, String group);
    void deleteQuestion(String question);
    void deleteAllQuestions();
    String getAllQuestionsContent();
    List<Questions> getAll();
    boolean setIsMorning(String question, String isMorning);
    Long getMaxId();
    Long getMinId();
    List<Questions> getAllQuestions();
    List<Questions> getMorningQuestions(String group);
    List<Questions> getNotMorningQuestions(String group);
    Questions findFirstByMorningTrue(String group);
    Questions findFirstByMorningFalse(String group);
}
