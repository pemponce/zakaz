package com.example.telegrambot.service;

import com.example.telegrambot.model.BanQuestions;
import com.example.telegrambot.model.Questions;

import java.util.List;

public interface BanQuestionsService {
    Long getQuestionsLength();
    BanQuestions getQuestion(Long id);
    void deleteQuestion(String question);
    String getAllQuestions();
    boolean createQuestion(String question);
    void saveQuestion(BanQuestions question);
}
