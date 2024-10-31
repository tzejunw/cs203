package com.java.firebase.demo.tournament;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import io.github.cdimascio.dotenv.Dotenv;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutionException;
import com.google.cloud.firestore.FirestoreException;

@Service
public class TournamentValidator {

    private Firestore firestore;

    public TournamentValidator(Firestore firestore) {
        this.firestore = firestore;
    }

    public static boolean isValidDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            // Parse the date string using the formatter
            LocalDate date = LocalDate.parse(dateStr, formatter);
            // Ensure the parsed date string matches the input (to avoid things like 2024-2-30)
            return dateStr.equals(date.format(formatter));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean areDatesInOrder(String startDateStr, String endDateStr) {
        // Parse the dates after confirming they are in the correct format
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        // Check that the end date is not earlier than the start date
        return !endDate.isBefore(startDate);
    }

    public boolean isNameUnique(String name) throws InterruptedException, ExecutionException, FirestoreException {
        try {
            // Get the document from Firestore
            ApiFuture<DocumentSnapshot> future = firestore.collection("tournament").document(name).get();
            DocumentSnapshot document = future.get();

            // Return true if the document does NOT exist (ID is unique), otherwise false
            return !document.exists();
        } catch (InterruptedException | ExecutionException | FirestoreException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isValidAddress(String address) {
        Dotenv dotenv = Dotenv.load();
        String map_api_key = dotenv.get("GOOGLE_MAP_API_KEY");
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + map_api_key;

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                
                if (responseBody != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode json = mapper.readTree(responseBody);
                    String status = json.get("status").asText();

                    // Check if the status is OK, indicating the address is valid
                    System.out.println("OK".equals(status));
                    return "OK".equals(status);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isTournamentValid(Tournament tournament) throws IllegalArgumentException, InterruptedException, ExecutionException, FirestoreException {
        if (!isValidDate(tournament.getStartDate()))
            throw new IllegalArgumentException("Start date should be in yyyy-MM-dd format");
        if (!isValidDate(tournament.getEndDate()))
            throw new IllegalArgumentException("End date should be in yyyy-MM-dd format");
        if (!isValidDate(tournament.getRegistrationDeadline()))
            throw new IllegalArgumentException("End date should be in yyyy-MM-dd format");
        if (!areDatesInOrder(tournament.getStartDate(), tournament.getEndDate())){
            throw new IllegalArgumentException("End date should not be earlier than the start date");
        }
        if (!areDatesInOrder(tournament.getRegistrationDeadline(), tournament.getStartDate())){
            throw new IllegalArgumentException("Start date should not be earlier than the registration dateline");
        }
        if (Strings.isNullOrEmpty(tournament.getTournamentDesc())){
            throw new IllegalArgumentException("Tournament description should not be empty");
        }
        // if (Strings.isNullOrEmpty(tournament.getImageUrl())){
        //     throw new IllegalArgumentException("Image should not be empty");
        // }
        if (Strings.isNullOrEmpty(tournament.getLocation()) || !isValidAddress(tournament.getLocation())){
            throw new IllegalArgumentException("Invalid location.");
        }
        if (Strings.isNullOrEmpty(tournament.getTournamentName())){
            throw new IllegalArgumentException("Invalid tournament name.");
        }
        return true;
    }


    
}
