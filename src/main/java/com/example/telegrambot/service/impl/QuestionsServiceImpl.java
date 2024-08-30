package com.example.telegrambot.service.impl;

import com.example.telegrambot.model.Questions;
import com.example.telegrambot.repository.QuestionsRepository;
import com.example.telegrambot.service.QuestionsService;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@AllArgsConstructor
public class QuestionsServiceImpl implements QuestionsService {

    private QuestionsRepository questionsRepository;

    @Override
    public Long getQuestionsLength() {
        return (long) questionsRepository.getAllQuestions().size();
    }

    @Override
    public String getQuestion(Long id) {
        return questionsRepository.getQuestionsById(id).getQuestion();
    }

    @Override
    public Questions createQuestion(String question) {
        Questions newQuestion = Questions.builder()
                .question(question)
                .build();
        return questionsRepository.save(newQuestion);
    }

    @Override
    public String getAllQuestions() {
        String res = "";
        List<Questions> questions = new ArrayList<>(questionsRepository.findAll());
        int counter = 1;
        for(Questions question:questions) {
            res += counter + " - " + question.getQuestion() + "\n";
            counter++;
        }
        return res;
    }
}
