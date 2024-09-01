package com.example.telegrambot.googleSheets.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetsService {

//    @Value(value = "${spring.application.name}")
    private static final String APPLICATION_NAME = "telegramBot";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json"; // Укажи путь к JSON с учетными данными

    @SneakyThrows
    private static Credential getCredentials() {
        InputStream in = GoogleSheetsService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("online")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public void addDataToGoogleSheet(String spreadsheetId, String range, List<List<Object>> values) throws IOException, GeneralSecurityException {
        final Sheets sheetsService = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange body = new ValueRange().setValues(values);
        sheetsService.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
    }
    public void createSheet(String spreadsheetId, String sheetName) throws IOException, GeneralSecurityException {
        final Sheets sheetsService = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();

        AddSheetRequest addSheetRequest = new AddSheetRequest().setProperties(new SheetProperties().setTitle(sheetName));
        Request request = new Request().setAddSheet(addSheetRequest);
        BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest().setRequests(Collections.singletonList(request));
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchRequest).execute();
    }

}
