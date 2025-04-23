package com.example.telegrambot.service;

import com.example.telegrambot.model.Group;

import java.util.Optional;

public interface GroupService {
    void create(Group group);

    Optional<Group> findByName(String name);

    Group getByName(String name);
}
