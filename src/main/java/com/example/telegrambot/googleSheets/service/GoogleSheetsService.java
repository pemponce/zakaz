package com.example.telegrambot.googleSheets.service;

import ch.qos.logback.classic.net.server.ServerSocketReceiver;
import com.example.telegrambot.service.impl.LocalServerReceiverImpl;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "telegramBot";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    @Value("${google.sheets.spreadsheetId}")
    private String spreadsheetId;

    private Sheets.Spreadsheets spreadsheets;

    @PostConstruct
    @SneakyThrows
    private void init() {
        var in = getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (Objects.isNull(in)) {
            throw new FileNotFoundException(CREDENTIALS_FILE_PATH.concat(" not found!"));
        }
        var client = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        var receiver = new LocalServerReceiverImpl.Builder()
//                .setHost("redirectmeto.com/http://185.209.162.213")
                .setPort(8888)
                .build();
        var flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                client,
                SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("online")
                .build();
        var credentials = new AuthorizationCodeInstalledApp(flow, receiver)
                .authorize("user");
        spreadsheets = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build()
                .spreadsheets();
        createSheetsIfNotExists();
    }

    private void createSheetsIfNotExists() {
        createList("usersCode", "");
        createList("Banned", "");
    }

    public void createList(String sheetName, String group) {
        executionWrapper(() -> {
            var addSheetRequest = new AddSheetRequest().setProperties(new SheetProperties().setTitle(sheetName+group));
            var request = new Request().setAddSheet(addSheetRequest);
            var batchRequest = new BatchUpdateSpreadsheetRequest().setRequests(Collections.singletonList(request));
            return spreadsheets.batchUpdate(spreadsheetId, batchRequest).execute();
        });
    }

    public void addData(String range, List<List<Object>> values) {
        executionWrapper(() -> {
            var body = new ValueRange()
                    .setValues(values);
            return spreadsheets.values()
                    .append(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();
        });
    }

    public void updateData(String range, List<List<Object>> values) {
        executionWrapper(() -> {
            var body = new ValueRange()
                    .setValues(values);
            return spreadsheets.values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();
        });
    }


    private interface GoogleSheetsExecution<T> {
        T execute() throws IOException;
    }

    private <T> T executionWrapper(GoogleSheetsExecution<T> execution) {
        try {
            return execution.execute();
        } catch (GoogleJsonResponseException exception) {
            var errors = exception.getDetails().getErrors().stream()
                    .map(GoogleJsonError.ErrorInfo::getMessage)
                    .collect(Collectors.joining("\n"));
            log.error("Can't execute request. \nErrors: \n{}", errors);
        } catch (IOException exception) {
            log.error("Can't execute request", exception);
        }
        return null;
    }
}