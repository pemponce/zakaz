package com.example.telegrambot.service.impl;

import com.example.telegrambot.model.Questions;
import com.example.telegrambot.repository.QuestionsRepository;
import com.example.telegrambot.service.QuestionsService;
import com.fasterxml.jackson.databind.node.LongNode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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
    public boolean createQuestion(String question, String group) {
        boolean flag = false;
        if (!questionsRepository.existsByQuestion(question) || questionsRepository.findAll().isEmpty()) {
            Questions newQuestion = Questions.builder()
                    .question(question)
                    .questionGroup(group)
                    .relevant(true)
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
    public void deleteAllQuestions() {
        try {
            var allQuestions = getAllQuestions().stream().map(Questions::getId).toList();
            for (Long id: allQuestions) {

                questionsRepository.deleteById(id);
            }
        } catch (Exception e) {
            throw new RuntimeException("Список вопросов пуст", e);
        }
    }

    @Override
    public String getAllQuestionsContent() {
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

    @Override
    public List<Questions> getAllQuestions() {
        return questionsRepository.findAll();
    }

    @Override
    public List<Questions> getAllQuestionsByRelevantTrue() {
        return questionsRepository.findAllByRelevantTrue();
    }

    public List<Questions> getMorningQuestions(String group) {
        return questionsRepository.findByMorningTrueAndRelevantTrueAndQuestionGroup(group);
    }

    @Override
    public List<Questions> getNotMorningQuestions(String group) {
        return questionsRepository.findByMorningFalseAndRelevantTrueAndQuestionGroup(group);
    }

    @Override
    public String questionListToString(List<Questions> questions) {
        var text = "";
        text += IntStream.range(0, questions.size()).mapToObj(
                i -> (i + 1) + " - " + questions.get(i).getQuestion()
        ).collect(Collectors.joining("\n"));
        return text;
    }

    @Override
    public Questions findFirstByMorningTrue(String group) {
        return questionsRepository.findFirstByMorningTrueAndRelevantTrueAndQuestionGroupOrderByIdAsc(group);
    }

    @Override
    public Questions findFirstByMorningFalse(String group) {
        return questionsRepository.findFirstByMorningFalseAndRelevantTrueAndQuestionGroupOrderByIdAsc(group);
    }

    @Override
    public Questions save(Questions question) {
        return questionsRepository.save(question);
    }
}
