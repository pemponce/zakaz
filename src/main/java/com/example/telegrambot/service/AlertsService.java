package com.example.telegrambot.service;

import com.example.telegrambot.model.Alerts;
import com.example.telegrambot.model.Users;

import java.util.List;

public interface AlertsService {
    void createAlert(String content, String group);
    void deleteAlert(String content, String group);
    Alerts getAlert(String content, String group);
    Alerts getLastGroupAlert(String group);
    String getGroupAlertsFalse(String group, Users user);
    List<Alerts> getAllAlerts(String group);
    String getAllAlertsContent(String group, Users user);
    void save(Alerts alerts);
}
