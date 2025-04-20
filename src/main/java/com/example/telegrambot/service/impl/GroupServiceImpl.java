package com.example.telegrambot.service.impl;

import com.example.telegrambot.model.Group;
import com.example.telegrambot.repository.GroupRepository;
import com.example.telegrambot.service.GroupService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    @Override
    public void create(Group group) {
        groupRepository.save(group);
    }

    @Override
    public Optional<Group> findByName(String name) {
        return groupRepository.findByName(name);
    }

    @Override
    public Group getByName(String name) {
        return groupRepository.getByName(name);
    }
}
