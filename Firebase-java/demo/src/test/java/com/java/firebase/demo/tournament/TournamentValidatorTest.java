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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class TournamentValidatorTest {

    @Mock
    private Firestore firestore;

    @Mock
    private TournamentService tournamentService; // Mock TournamentService

    @InjectMocks
    private TournamentValidator tournamentValidator; // Class under test

    private Tournament validTournament;

    @BeforeEach
    public void setUp() {
        validTournament = new Tournament();
        validTournament.setTournamentName("UniqueTournament");
        validTournament.setStartDate("2024-10-06");
        validTournament.setEndDate("2024-10-06");
        validTournament.setLocation("One Infinite Loop, Cupertino, CA 95014, USA");
        validTournament.setImageUrl( "https://firebasestorage.googleapis.com/v0/b/cs203-a263b.appspot.com/o/tournament-3.jpg?alt=media& token=fd0562ea-0dd9-4e8d-9cd0-54a78f9ac30d");
        validTournament.setTournamentDesc("unit test");
        validTournament.setRegistrationDeadline("2024-10-04");
        
        
    }




    
}
