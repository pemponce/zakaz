package com.example.telegrambot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Optional;
import java.util.stream.Stream;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Questions {

    /*
    TODO: Добавить еще одно поле isRelevant (является ли актуальным вопросом, или нет)
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;
    private String questionGroup;

    private boolean morning;
    private boolean isRelevant;

}
