package com.example.telegrambot.service.impl;

import com.example.telegrambot.model.Alerts;
import com.example.telegrambot.model.Emoji;
import com.example.telegrambot.model.Role;
import com.example.telegrambot.model.Users;
import com.example.telegrambot.repository.AlertsRepository;
import com.example.telegrambot.service.AlertsService;
import com.example.telegrambot.service.GroupService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class AlertServiceImpl implements AlertsService {

    private final AlertsRepository alertsRepository;
    private final GroupService groupService;

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
    public Alerts getLastGroupAlertFalse(String group) {
        return alertsRepository.findTopByAlertGroupAndActiveFalseOrderByIdDesc(group);
    }

    @Override
    public String getGroupAlertsFalse(String group, Users user) {
        List<Alerts> alerts = alertsRepository.findAllByAlertGroupAndActiveFalse(group);

        return contentToString(alerts, group, user);
    }

    @Override
    public List<Alerts> getAllAlerts(String group) {
        return alertsRepository.findAllByAlertGroup(group);
    }

    @Override
    public String getAllAlertsContent(String group, Users user) {
        List<Alerts> alerts = getAllAlerts(group);

        return contentToString(alerts, group, user);
    }

    @Override
    public void save(Alerts alerts) {
        alertsRepository.save(alerts);
    }

    public String contentToString(List<Alerts> alerts, String group, Users user) {

        var messageText = "";

        if (alerts.size() > 0) {
            messageText += Emoji.ALERT.getData() + "Оповещения для группы - " + group;
            if (user.getRole().equals(Role.ADMIN)) {
                messageText += "\n(https://docs.google.com/spreadsheets/d/" + groupService.findByName(group).get().getSpreadsheetId() + ")\n";
            }
            messageText += IntStream.range(0, alerts.size())
                    .mapToObj(i -> (alerts.get(i).isActive() ?
                            Emoji.ALARM.getData() + " " : Emoji.CHECKED.getData() + " ")
                            + (i + 1) + " - " + alerts.get(i).getContent()
                    )
                    .collect(Collectors.joining("\n"));
        } else {
            messageText += Emoji.WARNING.getData() + "Нет оповещений для группы - " + group;
            if (user.getRole().equals(Role.ADMIN)) {
                messageText += "\n (https://docs.google.com/spreadsheets/d/" + groupService.findByName(group).get().getSpreadsheetId() + ")\n";
            }
        }
        return messageText;
    }
}
