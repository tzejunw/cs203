package com.java.firebase.demo.tournament;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.common.base.Strings;

import io.github.cdimascio.dotenv.Dotenv;

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
        final String GOOGLE_MAP_API_KEY = loadApiKey();
        // Build the URL for the Google Maps Geocoding API request
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + GOOGLE_MAP_API_KEY;
    
        try {
            // Fetch the response from the Geocoding API
            ResponseEntity<String> response = fetchGeocodeResponse(url);
            // Check if the response indicates a valid address
            return isResponseValid(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String loadApiKey() {
        // Load the Google Maps API key from environment variables
        Dotenv dotenv = Dotenv.load();
        return dotenv.get("GOOGLE_MAP_API_KEY");
    }
    
    private ResponseEntity<String> fetchGeocodeResponse(String url) {
        // Use RestTemplate to send a GET request to the provided URL and retrieve the response
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(url, String.class);
    }
    
    private boolean isResponseValid(ResponseEntity<String> response) throws Exception {
        // Check if the response code indicates success (2xx) and has a body
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return isStatusOk(response.getBody());
        }
        return false;
    }
    
    private boolean isStatusOk(String responseBody) throws Exception {
        // Parse the JSON response body
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(responseBody);
        // Extract the "status" field and check if it's "OK"
        String status = json.get("status").asText();
        return "OK".equals(status);
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
        if (Strings.isNullOrEmpty(tournament.getLocation()) || !isValidAddress(tournament.getLocation())){
            throw new IllegalArgumentException("Invalid location.");
        }
        if (Strings.isNullOrEmpty(tournament.getTournamentName())){
            throw new IllegalArgumentException("Invalid tournament name.");
        }
        return true;
    }

}
