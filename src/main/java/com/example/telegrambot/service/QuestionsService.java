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
    String getAllQuestionsContent(String group);
    List<Questions> getAll();
    boolean setIsMorning(String question, String isMorning);
    Long getMaxId();
    Long getMinId();
    List<Questions> getAllQuestions();
    List<Questions> getAllQuestionsByRelevantTrue();
    List<Questions> getMorningQuestions(String group);
    List<Questions> getNotMorningQuestions(String group);
    String questionListToString(List<Questions> questions);
    Questions findFirstByMorningTrue(String group);
    Questions findFirstByMorningFalse(String group);
    Questions save(Questions question);
}
