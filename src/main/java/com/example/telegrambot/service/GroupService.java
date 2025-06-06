package com.example.telegrambot.service;

import com.example.telegrambot.model.Group;

import java.util.List;
import java.util.Optional;

public interface GroupService {
    void create(Group group);
    List<Group> findAllGroups();
    Optional<Group> findByName(String name);
    Group getByName(String name);
}
