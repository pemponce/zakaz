package com.example.telegrambot.quesionsEnum;

import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
public enum Questions {
    HOW_WAS_UR_DAY("КАК ПРОШЕЛ ДЕНЬ?", 1),
    WHAT_DID_U_EAT("ЧТО ТЫ ЖРАЛ?", 2),
    WEDDING("ПОЖЕНИМСЯ?", 3);

    private final String display;
    private final int value;
    Questions(String display, int value) {
        this.display = display;
        this.value = value;
    }

    public static String getQuestionViaValue(int value) {
        Optional<Questions> questionsOptional = Stream.of(values()).filter(questions -> value == questions.value).findAny();
        return questionsOptional.get().getDisplay();
    }
}
