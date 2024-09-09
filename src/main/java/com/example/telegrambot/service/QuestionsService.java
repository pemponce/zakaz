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
    boolean setIsMorning(String question, String isMorning);
    Long getMaxId();
    Long getMinId();
    List<Questions> getMorningQuestions();
    List<Questions> getNotMorningQuestions();
    Questions findFirstByMorningTrue();
    Questions findFirstByMorningFalse();

    void saveQuestion(Questions question);
}
