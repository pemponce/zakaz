package com.example.telegrambot.repository;

import com.example.telegrambot.model.Alerts;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertsRepository extends JpaRepository<Alerts, Long> {

    List<Alerts> findAllByAlertGroupAndActiveFalse(String group);
    List<Alerts> findAllByAlertGroup(String group);
    Alerts findTopByAlertGroupAndActiveTrue(String group);
    Alerts findByContentAndAlertGroupAndActiveFalse(String content, String group);
    Alerts findByContentAndAlertGroup(String content, String group);
    void deleteById(@NotNull Long id);

}
