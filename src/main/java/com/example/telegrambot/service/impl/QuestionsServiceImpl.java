package com.example.telegrambot.service.impl;

import com.example.telegrambot.model.Questions;
import com.example.telegrambot.repository.QuestionsRepository;
import com.example.telegrambot.service.QuestionsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class QuestionsServiceImpl implements QuestionsService {

    private QuestionsRepository questionsRepository;

    @Override
    public Long getQuestionsLength() {
        return (long) questionsRepository.getAllQuestions().size();
    }

    @Override
    public Questions getQuestion(Long id) {
        return questionsRepository.getQuestionsById(id);
    }

    @Override
    public Questions createQuestion(String question) {
        Questions newQuestion = Questions.builder()
                .question(question)
                .active(false)
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

    @Override
    public Optional<Questions> findActiveQuestion() {
        return questionsRepository.findFirstByActiveTrue();
    }

    @Override
    public void saveQuestion(Questions question) {
        // Сохранение вопроса в базе данных
        questionsRepository.save(question);
    }


}
