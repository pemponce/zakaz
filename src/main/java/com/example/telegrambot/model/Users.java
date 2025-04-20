package com.example.telegrambot.model;

import com.example.telegrambot.model.enumRole.Role;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private int verificationCode;
    private boolean isVerify;
    private Role role;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @JoinColumn(table = "user_chat")
    private Long chatId;
}

