package com.example.telegrambot.service;

import com.example.telegrambot.model.Alerts;

import java.util.List;

public interface AlertsService {
    void createAlert(String content, String group);
    void deleteAlert(String content, String group);
    Alerts getAlert(String content, String group);
    List<Alerts> getAllAlerts(String group);
}
