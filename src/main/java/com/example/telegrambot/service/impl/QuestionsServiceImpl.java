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
    public boolean createQuestion(String question) {
        boolean flag = false;
        if (!questionsRepository.existsByQuestion(question) || questionsRepository.findAll().isEmpty()) {
            Questions newQuestion = Questions.builder()
                    .question(question)
                    .active(false)
                    .build();
            questionsRepository.save(newQuestion);
            flag = true;
        }
        return flag;
    }

    @Override
    public void deleteQuestion(String question) {

        long questionId = questionsRepository.getQuestionsByQuestion(question).getId();

        questionsRepository.deleteById(questionId);
    }

    @Override
    public String getAllQuestions() {
        String res = "";
        List<Questions> questions = new ArrayList<>(questionsRepository.findAll());
        int counter = 1;
        for (Questions question : questions) {
            res += counter + " - " + question.getQuestion() + "\n";
            counter++;
        }
        return res;
    }

    @Override
    public List<Questions> getAll() {
        return questionsRepository.findAll();
    }

    @Override
    public Long getMaxId() {
        return questionsRepository.getMaxId();
    }

    @Override
    public Long getMinId() {
        return questionsRepository.getMinId();
    }

    @Override
    public Optional<Questions> findActiveQuestion() {
        return questionsRepository.findFirstByActiveTrue();
    }

    @Override
    public void saveQuestion(Questions question) {
        questionsRepository.save(question);
    }


}
