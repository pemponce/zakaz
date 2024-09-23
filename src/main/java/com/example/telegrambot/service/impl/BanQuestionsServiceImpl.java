package com.example.telegrambot.service.impl;

import com.example.telegrambot.googleSheets.service.GoogleSheetsService;
import com.example.telegrambot.model.BanQuestions;
import com.example.telegrambot.repository.BanQuestionsRepository;
import com.example.telegrambot.service.BanQuestionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BanQuestionsServiceImpl implements BanQuestionsService {
    @Autowired
    private BanQuestionsRepository banQuestionsRepository;

    @Autowired
    private GoogleSheetsService googleSheetsService;

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

            googleSheetsService.updateData("Banned!B1:Z1", prepareForUpdateSheets());
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

        googleSheetsService.updateData("Banned!B1:Z1", prepareForUpdateSheets());

    }

    @Override
    public String getAllQuestions() {
        StringBuilder res = new StringBuilder();
        List<BanQuestions> questions = new ArrayList<>(banQuestionsRepository.findAll());
        int counter = 1;
        for (BanQuestions question : questions) {
            res.append(counter).append(" - ").append(question.getQuestion()).append("\n");
            counter++;
        }
        return res.toString();
    }

    @Override
    public List<BanQuestions> getAll() {
        return banQuestionsRepository.findAll();
    }

    @Override
    public void saveQuestion(BanQuestions question) {
        banQuestionsRepository.save(question);
    }

    @Override
    public List<List<Object>> prepareForUpdateSheets() {
        List<BanQuestions> allQuestions = banQuestionsRepository.findAll();

        List<List<Object>> values = new ArrayList<>();
        List<Object> row = new ArrayList<>();
        for (BanQuestions q : allQuestions) {
            row.add(q.getQuestion());
        }
        row.add("");
        values.add(row);

        return values;
    }

}
