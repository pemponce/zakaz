package com.example.telegrambot.service.impl;

import com.example.telegrambot.model.Alerts;
import com.example.telegrambot.repository.AlertsRepository;
import com.example.telegrambot.service.AlertsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AlertServiceImpl implements AlertsService {

    private final AlertsRepository alertsRepository;

    @Override
    public void createAlert(String content, String group) {
        Alerts alert = Alerts.builder()
                .content(content)
                .alertGroup(group)
                .active(true)
                .build();
        alertsRepository.save(alert);
    }

    @Override
    public void deleteAlert(String content, String group) {
        var alert = getAlert(content, group);
        alertsRepository.deleteById(alert.getId());
    }

    @Override
    public Alerts getAlert(String content, String group) {
        return alertsRepository.findByContentAndAlertGroup(content, group);
    }

    @Override
    public Alerts getLastGroupAlert(String group) {
        return alertsRepository.findTopByAlertGroupAndActiveTrue(group);
    }

    @Override
    public List<Alerts> getAllAlerts(String group) {
        return alertsRepository.findAllByAlertGroup(group);
    }
}
