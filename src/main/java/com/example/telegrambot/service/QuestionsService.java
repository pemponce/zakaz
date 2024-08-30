package com.example.telegrambot.service;

import com.example.telegrambot.model.Questions;

import java.util.List;

public interface QuestionsService {
    Long getQuestionsLength();
    String getQuestion(Long id);
    Questions createQuestion(String question);
    String getAllQuestions();
}
