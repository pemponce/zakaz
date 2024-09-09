package com.example.telegrambot.service.impl;

import com.example.telegrambot.model.BanQuestions;
import com.example.telegrambot.model.Questions;
import com.example.telegrambot.repository.BanQuestionsRepository;
import com.example.telegrambot.repository.QuestionsRepository;
import com.example.telegrambot.service.BanQuestionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BanQuestionsServiceImpl implements BanQuestionsService {
    @Autowired
    private BanQuestionsRepository banQuestionsRepository;

    @Override
    public Long getQuestionsLength() {
        return (long) banQuestionsRepository.getAllBanQuestions().size();
    }

    @Override
    public BanQuestions getQuestion(Long id) {
        return banQuestionsRepository.getBanQuestionsById(id);
    }

    @Override
    public boolean createQuestion(String question) {
        boolean flag = false;
        if (!banQuestionsRepository.existsByQuestion(question) || banQuestionsRepository.findAll().isEmpty()) {
            BanQuestions newQuestion = BanQuestions.builder()
                    .question(question)
                    .build();
            banQuestionsRepository.save(newQuestion);
            flag = true;
        }
        return flag;
    }

    @Override
    public Long getMaxId() {
        return banQuestionsRepository.getMaxId();
    }

    @Override
    public Long getMinId() {
        return banQuestionsRepository.getMinId();
    }

    @Override
    public void deleteQuestion(String question) {

        long questionId = banQuestionsRepository.getBanQuestionsByQuestion(question).getId();

        banQuestionsRepository.deleteById(questionId);
    }

    @Override
    public String getAllQuestions() {
        String res = "";
        List<BanQuestions> questions = new ArrayList<>(banQuestionsRepository.findAll());
        int counter = 1;
        for (BanQuestions question : questions) {
            res += counter + " - " + question.getQuestion() + "\n";
            counter++;
        }
        return res;
    }

    @Override
    public List<BanQuestions> getAll() {
        return banQuestionsRepository.findAll();
    }

    @Override
    public void saveQuestion(BanQuestions question) {
        banQuestionsRepository.save(question);
    }

}
