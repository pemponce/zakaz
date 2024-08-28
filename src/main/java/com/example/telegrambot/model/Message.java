package com.example.telegrambot.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(table = "user_chat")
    private Long userId;

    @JoinColumn(table = "user_chat")
    private Long chatId;

    @JoinColumn(table = "users")
    private String userName;

    @Column(name = "response_message")
    private String response_message;

//    private String question;

    private String time;

}
