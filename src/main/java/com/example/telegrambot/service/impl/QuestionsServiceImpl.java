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
                    .morning(false)
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
        String time = "";
        for (Questions question : questions) {
            if (question.isMorning()) {
                time = "10:30";
            } else {
                time = "23:50";
            }
            res += counter + " - " + question.getQuestion() + " (" + time + ")" + "\n";
            counter++;
        }
        return res;
    }

    @Override
    public List<Questions> getAll() {
        return questionsRepository.findAll();
    }

    @Override
    public boolean setIsMorning(String question, String isMorning) {
        boolean flag = true;

        if(questionsRepository.getQuestionsByQuestion(question) != null) {
            Questions currQuestion = questionsRepository.getQuestionsByQuestion(question);

            switch (isMorning.toLowerCase()) {
                case "день" -> {
                    currQuestion.setMorning(true);
                    questionsRepository.save(currQuestion);
                }
                case "вечер" -> {
                    currQuestion.setMorning(false);
                    questionsRepository.save(currQuestion);
                }
                default -> flag = false;
            }
        } else {
            flag = false;
        }
        return flag;
    }

    @Override
    public Long getMaxId() {
        return questionsRepository.getMaxId();
    }

    @Override
    public Long getMinId() {
        return questionsRepository.getMinId();
    }

    public List<Questions> getMorningQuestions() {
        return questionsRepository.findByMorningTrue();
    }

    @Override
    public List<Questions> getNotMorningQuestions() {
        return questionsRepository.findByMorningFalse();
    }

    @Override
    public Questions findFirstByMorningTrue() {
        return questionsRepository.findFirstByMorningTrueOrderByIdAsc();
    }

    @Override
    public Questions findFirstByMorningFalse() {
        return questionsRepository.findFirstByMorningFalseOrderByIdAsc();
    }

    @Override
    public void saveQuestion(Questions question) {
        questionsRepository.save(question);
    }


}
