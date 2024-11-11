package com.java.firebase.demo.tournament;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.java.firebase.demo.algo.AlgoRound;


@ExtendWith(MockitoExtension.class)
public class TournamentServiceTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private CollectionReference tournamentCollection;

    @Mock
    private CollectionReference roundCollection;

    @Mock
    private DocumentReference tournamentDocument;

    @Mock
    private DocumentReference roundDocument;

    @Mock
    private DocumentReference matchDocument;

    @Mock
    private DocumentReference playerDocument;

    @Mock
    private ApiFuture<WriteResult> apiFutureWriteResult;

    @Mock
    ApiFuture<DocumentSnapshot> apiFutureDocumentSnapshot;

    @Mock
    ApiFuture<QuerySnapshot> apiFutureQuerySnapshot;

    @Mock
    DocumentSnapshot documentSnapshot;

    @Mock
    QuerySnapshot querySnapshot;

    @Mock
    TournamentValidator tournamentValidator;

    @Mock
    WriteResult writeResult;

    @Mock
    Timestamp timeStamp;

    @Spy
    @InjectMocks
    private TournamentService tournamentService;

    private Tournament validTournament;

    @BeforeEach
    void setup() throws InterruptedException, ExecutionException {
        validTournament = new Tournament();
        validTournament.setTournamentName("UniqueTournament");
        validTournament.setStartDate("2024-10-06");
        validTournament.setEndDate("2024-10-06");
        validTournament.setLocation("One Infinite Loop, Cupertino, CA 95014, USA");
        validTournament.setImageUrl( "https://firebasestorage.googleapis.com/v0/b/cs203-a263b.appspot.com/o/tournament-3.jpg?alt=media& token=fd0562ea-0dd9-4e8d-9cd0-54a78f9ac30d");
        validTournament.setTournamentDesc("unit test");
        validTournament.setRegistrationDeadline("2024-10-04");
        
    }

    @Test
    void createTournament_ValidAndUniqueTournament_ReturnsUpdateTime() throws ExecutionException, InterruptedException {
        // Arrange
        // Create a mock Timestamp and set it to return a string when toString() is called
        Timestamp timeStamp = mock(Timestamp.class);
        when(timeStamp.toString()).thenReturn("date");
    
        // Mocking the WriteResult to return the mock Timestamp
        when(writeResult.getUpdateTime()).thenReturn(timeStamp);
        
        // Set up the collection future to return the mocked writeResult
        when(firestore.collection("tournament")).thenReturn(tournamentCollection);

        when(tournamentCollection.document(validTournament.getTournamentName())).thenReturn(tournamentDocument);

        when(tournamentDocument.set(validTournament)).thenReturn(apiFutureWriteResult);

        when(apiFutureWriteResult.get()).thenReturn(writeResult);
        
        // Mock tournamentValidator methods
        when(tournamentValidator.isTournamentValid(validTournament)).thenReturn(true);
        when(tournamentValidator.isNameUnique(validTournament.getTournamentName())).thenReturn(true);
    
        // Act
        String result = tournamentService.createTournament(validTournament);
    
        // Assert
        assertNotNull(result);
        assertEquals("date", result);  // Verify the returned date string matches expected value
        verify(firestore.collection("tournament").document(validTournament.getTournamentName()), times(1)).set(validTournament);
    }
    

    @Test
    void createTournament_InvalidTournament_ThrowsIllegalArgumentException() throws ExecutionException, InterruptedException {
        // Arrange
        Tournament invalidTournament = new Tournament();
        invalidTournament.setTournamentName(null); 
        when(tournamentValidator.isTournamentValid(invalidTournament)).thenThrow(new IllegalArgumentException("Invalid tournament name."));
        //when(tournamentValidator.isTournamentValid(invalidTournament)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.createTournament(invalidTournament);
        });
        assertEquals("Invalid tournament name.", exception.getMessage());
    }

    @Test
    void createTournament_NonUniqueTournament_ThrowsIllegalArgumentException() throws ExecutionException, InterruptedException {
        // Arrange
        when(tournamentValidator.isTournamentValid(validTournament)).thenReturn(true);
        when(tournamentValidator.isNameUnique(validTournament.getTournamentName())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.createTournament(validTournament);
        });
        assertEquals("The same tournament name already exists.", exception.getMessage());
    }


    @Test
    void fetchTournament_ValidTournamentName_ReturnsTournament() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "TestTournament";
        Tournament expectedTournament = new Tournament();
        // Mock the Firestore call to return a document
        when(firestore.collection("tournament")).thenReturn(tournamentCollection);
        when(tournamentCollection.document(tournamentName)).thenReturn(tournamentDocument);
        when(firestore.collection("tournament").document(tournamentName).get()).thenReturn(apiFutureDocumentSnapshot);
        when(apiFutureDocumentSnapshot.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(Tournament.class)).thenReturn(expectedTournament);

        // Act
        Tournament actualTournament = tournamentService.fetchTournament(tournamentName);

        // Assert
        assertEquals(expectedTournament, actualTournament);
    }

    @Test
    void fetchRounds_ValidTournamentName_ReturnsRounds() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "TestTournament";
        
        // Mock the Firestore interaction chain
        when(firestore.collection("tournament")).thenReturn(tournamentCollection);
        when(tournamentCollection.document(tournamentName)).thenReturn(tournamentDocument);
        when(tournamentDocument.collection("round")).thenReturn(roundCollection);

        // Mock the ApiFuture for the collection get() method
        // ApiFuture<QuerySnapshot> roundsApiFuture = mock(ApiFuture.class);
        when(tournamentDocument.collection("round").get()).thenReturn(apiFutureQuerySnapshot);
        when(apiFutureQuerySnapshot.get()).thenReturn(querySnapshot);

        // Create mock round documents
        QueryDocumentSnapshot roundDoc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot roundDoc2 = mock(QueryDocumentSnapshot.class);
        when(roundDoc1.getId()).thenReturn("Round1");
        when(roundDoc2.getId()).thenReturn("Round2");

        // Set up the QuerySnapshot to return these mock round documents
        when(querySnapshot.isEmpty()).thenReturn(false);
        when(querySnapshot.size()).thenReturn(2);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(roundDoc1, roundDoc2));

        when(roundCollection.document("Round1")).thenReturn(roundDocument);
        when(roundCollection.document("Round2")).thenReturn(roundDocument);
        when(roundDocument.get()).thenReturn(apiFutureDocumentSnapshot);
        when(apiFutureDocumentSnapshot.get()).thenReturn(documentSnapshot);

        // Mock the getRound method to return dummy Round objects
        when(tournamentService.getRound(tournamentName, "Round1")).thenReturn(new Round("Round1"));
        when(tournamentService.getRound(tournamentName, "Round2")).thenReturn(new Round("Round2"));

        // Act
        List<Round> rounds = tournamentService.fetchRounds(tournamentName);

        // Assert
        assertNotNull(rounds);
        assertEquals(2, rounds.size());
        assertEquals("Round1", rounds.get(0).getRoundName());
        assertEquals("Round2", rounds.get(1).getRoundName());
    }

    @Test
    void fetchParticipatingPlayers_WithPlayers_ReturnsPlayerList() throws ExecutionException, InterruptedException {

                // Mock Firestore document and player collection
        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("tournament").document(anyString())).thenReturn(playerDocument);
        when(playerDocument.collection("participatingPlayers")).thenReturn(collectionReference);
        when(collectionReference.get()).thenReturn(apiFutureQuerySnapshot);
        // Arrange
        QueryDocumentSnapshot playerDoc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot playerDoc2 = mock(QueryDocumentSnapshot.class);

        when(playerDoc1.getId()).thenReturn("player1");
        when(playerDoc2.getId()).thenReturn("player2");

        when(apiFutureQuerySnapshot.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(false);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(playerDoc1, playerDoc2));

        // Act
        List<String> result = tournamentService.fetchParticipatingPlayers("SampleTournament");

        // Assert
        assertEquals(Arrays.asList("player1", "player2"), result);
    }

    @Test
    void fetchParticipatingPlayers_EmptyCollection_ReturnsEmptyList() throws ExecutionException, InterruptedException {
        // Arrange

                        // Mock Firestore document and player collection
        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("tournament").document(anyString())).thenReturn(playerDocument);
        when(playerDocument.collection("participatingPlayers")).thenReturn(collectionReference);
        when(collectionReference.get()).thenReturn(apiFutureQuerySnapshot);

        when(apiFutureQuerySnapshot.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(true);

        // Act
        List<String> result = tournamentService.fetchParticipatingPlayers("SampleTournament");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllTournaments_ReturnsTournamentList() throws ExecutionException, InterruptedException {
        // Arrange
        Tournament tournament1 = validTournament;
        Tournament tournament2 = new Tournament();

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        when(firestore.collection("tournament")).thenReturn(tournamentCollection);
        when(tournamentCollection.get()).thenReturn(apiFutureQuerySnapshot);
        when(apiFutureQuerySnapshot.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        // Mock document conversions to Tournament objects
        when(doc1.toObject(Tournament.class)).thenReturn(tournament1);
        when(doc2.toObject(Tournament.class)).thenReturn(tournament2);

        // Act
        List<Tournament> result = tournamentService.getAllTournaments();

        // Assert
        assertEquals(Arrays.asList(tournament1, tournament2), result);
    }

    @Test
    void getAllTournamentNames_ReturnsTournamentNameList() throws ExecutionException, InterruptedException {
        // Arrange
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        when(firestore.collection("tournament")).thenReturn(tournamentCollection);
        when(tournamentCollection.get()).thenReturn(apiFutureQuerySnapshot);
        when(apiFutureQuerySnapshot.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        when(doc1.getId()).thenReturn("Tournament1");
        when(doc2.getId()).thenReturn("Tournament2");

        // Act
        List<String> result = tournamentService.getAllTournamentNames();

        // Assert
        assertEquals(Arrays.asList("Tournament1", "Tournament2"), result);
    }

    @Test
    void updateTournament_TournamentExists_ReturnsUpdateMessage() throws ExecutionException, InterruptedException {
        // Arrange
        Tournament validTournament = new Tournament();
        validTournament.setTournamentName("SampleTournament");

        when(tournamentValidator.isTournamentValid(validTournament)).thenReturn(true);
        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("tournament").document(anyString())).thenReturn(tournamentDocument);
        when(tournamentDocument.get()).thenReturn(apiFutureDocumentSnapshot);
        when(apiFutureDocumentSnapshot.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(tournamentDocument.set(validTournament, SetOptions.merge())).thenReturn(apiFutureWriteResult);
        
        // Mock Timestamp
        Timestamp timeStamp = mock(Timestamp.class);
        when(timeStamp.toString()).thenReturn("date");
        when(apiFutureWriteResult.get()).thenReturn(mock(WriteResult.class));
        when(apiFutureWriteResult.get().getUpdateTime()).thenReturn(timeStamp);

        // Act
        String result = tournamentService.updateTournament(validTournament);

        // Assert
        assertEquals("Tournament updated at: date", result);
        verify(tournamentDocument, times(1)).set(validTournament, SetOptions.merge());
    }

    @Test
    void updateTournament_TournamentDoesNotExist_ReturnsNotFoundMessage() throws ExecutionException, InterruptedException {
        // Arrange
        Tournament validTournament = new Tournament();
        validTournament.setTournamentName("NonExistentTournament");

        when(tournamentValidator.isTournamentValid(validTournament)).thenReturn(true);
        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("tournament").document(anyString())).thenReturn(tournamentDocument);
        when(tournamentDocument.get()).thenReturn(apiFutureDocumentSnapshot);
        when(apiFutureDocumentSnapshot.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        // Act
        String result = tournamentService.updateTournament(validTournament);

        // Assert
        assertEquals("Tournament not found with name: NonExistentTournament", result);
        verify(tournamentDocument, never()).set(any(), any());
    }

    @Test
    void deleteTournament_DocumentExists_ReturnsSuccessMessage() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "SampleTournament";

        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        
        when(firestore.collection("tournament").document(tournamentName)).thenReturn(tournamentDocument);
        when(tournamentDocument.get()).thenReturn(apiFutureDocumentSnapshot);
        when(apiFutureDocumentSnapshot.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(tournamentDocument.delete()).thenReturn(apiFutureWriteResult);
        
        WriteResult writeResult = mock(WriteResult.class); // Mock WriteResult if you need to access more specific properties
        when(apiFutureWriteResult.get()).thenReturn(writeResult);

        // Act
        String result = tournamentService.deleteTournament(tournamentName);

        // Assert
        assertEquals("Successfully deleted " + tournamentName, result);
        verify(tournamentDocument, times(1)).delete();
    }

    @Test
    void deleteTournament_DocumentDoesNotExist_ReturnsNotFoundMessage() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "NonExistentTournament";

        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        
        when(firestore.collection("tournament").document(tournamentName)).thenReturn(tournamentDocument);
        when(tournamentDocument.get()).thenReturn(apiFutureDocumentSnapshot);
        when(apiFutureDocumentSnapshot.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        // Act
        String result = tournamentService.deleteTournament(tournamentName);

        // Assert
        assertEquals("Tournament not found: " + tournamentName, result);
        verify(tournamentDocument, never()).delete();
    }

    @Test
    void isTournamentInProgress_WithInProgressTournament_ReturnsTrue() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "SampleTournament";
        
        Tournament tournament = new Tournament();
        tournament.setInProgress(true); // Set tournament to "in-progress" state

        doReturn(tournament).when(tournamentService).getTournament(tournamentName);

        // Act
        boolean result = tournamentService.isTournamentInProgress(tournamentName);

        // Assert
        assertEquals(true, result);
        verify(tournamentService, times(1)).getTournament(tournamentName);
    }

    @Test
    void isTournamentInProgress_WithNotInProgressTournament_ReturnsFalse() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "SampleTournament";
        
        Tournament tournament = new Tournament();
        tournament.setInProgress(false); // Set tournament to "not in-progress" state

        doReturn(tournament).when(tournamentService).getTournament(tournamentName);
        // Act
        boolean result = tournamentService.isTournamentInProgress(tournamentName);

        // Assert
        assertEquals(false, result);
        verify(tournamentService, times(1)).getTournament(tournamentName);
    }

@Test
void isTournamentInProgress_WithNullTournament_ThrowsException() throws ExecutionException, InterruptedException {
    // Arrange
    String tournamentName = "NonExistingTournament";

    // Mock getTournament to return null
    doReturn(null).when(tournamentService).getTournament(tournamentName);

    // Act & Assert
    assertThrows(NoSuchElementException.class, () -> {
        tournamentService.isTournamentInProgress(tournamentName);
    });

    // Verify that getTournament was called
    verify(tournamentService, times(1)).getTournament(tournamentName);
}

    @Test
    void createRound_WithMatches_ReturnsSuccessMessage() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "SampleTournament";
        String roundName = "1";

        Match match1 = new Match("player1", "player2", "player1", 3, 0, false, false);
        Match match2 = new Match("player3", "player4", "player3", 2, 1, false, false);

        Round round = new Round(roundName, Arrays.asList(match1, match2));

        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("tournament").document(tournamentName)).thenReturn(tournamentDocument);
        when(tournamentDocument.collection("round")).thenReturn(mock(CollectionReference.class));

        when(tournamentDocument.collection("round").document(roundName)).thenReturn(roundDocument);
        when(roundDocument.set(anyMap())).thenReturn(apiFutureWriteResult);
        when(apiFutureWriteResult.get()).thenReturn(mock(WriteResult.class));
        
        // Mock match documents
        when(roundDocument.collection("match")).thenReturn(mock(CollectionReference.class));
        when(roundDocument.collection("match").document(anyString())).thenReturn(matchDocument);
        when(matchDocument.set(any(Match.class))).thenReturn(apiFutureWriteResult);

        // Act
        String result = tournamentService.createRound(tournamentName, round);

        // Assert
        assertEquals("Round 1, 2 matches, created in SampleTournament", result);
        verify(roundDocument, times(1)).set(anyMap());
        verify(matchDocument, times(2)).set(any(Match.class));
    }

    @Test
    void createRound_NoMatches_ReturnsSuccessMessage() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "SampleTournament";
        String roundName = "1";
        
        Round round = new Round(roundName, Collections.emptyList());  // No matches
    
        // Mock Firestore setup
        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("tournament").document(tournamentName)).thenReturn(tournamentDocument);
        when(tournamentDocument.collection("round")).thenReturn(mock(CollectionReference.class));
    
        when(tournamentDocument.collection("round").document(roundName)).thenReturn(roundDocument);
        when(roundDocument.set(anyMap())).thenReturn(apiFutureWriteResult);
        when(apiFutureWriteResult.get()).thenReturn(mock(WriteResult.class));

        when(roundDocument.collection("match")).thenReturn(mock(CollectionReference.class));

        // Act
        String result = tournamentService.createRound(tournamentName, round);
    
        // Assert
        assertEquals("Round 1, 0 matches, created in SampleTournament", result);
        verify(roundDocument, times(1)).set(anyMap());
        verify(roundDocument.collection("match"), never()).document(anyString());  // No matches, so this should not be called
    }

    @Test
    void getRound_WithExistingRoundAndMatches_ReturnsRound() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "SampleTournament";
        String roundName = "1";

        // Create a Round object with a list of matches
        Round expectedRound = new Round(roundName, Arrays.asList(
                new Match("player1", "player2", "player1", 3, 0, false, false),
                new Match("player3", "player4", "player3", 2, 1, false, false)
        ));

        // Mock Firestore setup
        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("tournament").document(tournamentName)).thenReturn(tournamentDocument);
        when(tournamentDocument.collection("round")).thenReturn(mock(CollectionReference.class));
        when(tournamentDocument.collection("round").document(roundName)).thenReturn(roundDocument);
        
        // Mock the round document
        when(roundDocument.get()).thenReturn(apiFutureDocumentSnapshot);
        when(apiFutureDocumentSnapshot.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(Round.class)).thenReturn(expectedRound);
        
        // Mock the match subcollection
        CollectionReference matchSubcollectionRef = mock(CollectionReference.class);
        when(roundDocument.collection("match")).thenReturn(matchSubcollectionRef);
        ApiFuture<QuerySnapshot> matchQuerySnapshotFuture = mock(ApiFuture.class);
        when(matchSubcollectionRef.get()).thenReturn(matchQuerySnapshotFuture);
        
        QuerySnapshot matchQuerySnapshot = mock(QuerySnapshot.class);
        when(matchQuerySnapshotFuture.get()).thenReturn(matchQuerySnapshot);
        when(matchQuerySnapshot.isEmpty()).thenReturn(false);
        when(matchQuerySnapshot.getDocuments()).thenReturn(Arrays.asList(
                mock(QueryDocumentSnapshot.class), 
                mock(QueryDocumentSnapshot.class)
        ));
        when(matchQuerySnapshot.getDocuments().get(0).toObject(Match.class)).thenReturn(expectedRound.getMatches().get(0));
        when(matchQuerySnapshot.getDocuments().get(1).toObject(Match.class)).thenReturn(expectedRound.getMatches().get(1));

        // Act
        Round result = tournamentService.getRound(tournamentName, roundName);

        // Assert
        assertNotNull(result);
        assertEquals(expectedRound.getRoundName(), result.getRoundName());
        assertEquals(expectedRound.getMatches().size(), result.getMatches().size());
    }

    @Test
    void getRound_RoundDoesNotExist_ReturnsNull() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "SampleTournament";
        String roundName = "1";

        // Mock Firestore setup
        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("tournament").document(tournamentName)).thenReturn(tournamentDocument);
        when(tournamentDocument.collection("round")).thenReturn(mock(CollectionReference.class));
        when(tournamentDocument.collection("round").document(roundName)).thenReturn(roundDocument);
        
        // Mock the round document
        when(roundDocument.get()).thenReturn(apiFutureDocumentSnapshot);
        when(apiFutureDocumentSnapshot.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);  // Simulate non-existent round

        // Act
        Round result = tournamentService.getRound(tournamentName, roundName);

        // Assert
        assertNull(result);
    }

    @Test
    void deleteRound_WithExistingRound_DeletesRoundAndReturnsSuccessMessage() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "SampleTournament";
        String roundName = "Round1";

        // Mock the Firestore structure
        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("tournament").document(tournamentName)).thenReturn(tournamentDocument);
        when(tournamentDocument.collection("round")).thenReturn(mock(CollectionReference.class));
        when(tournamentDocument.collection("round").document(roundName)).thenReturn(roundDocument);

        // Mock matches subcollection
        CollectionReference matchesSubcollection = mock(CollectionReference.class);
        when(roundDocument.collection("match")).thenReturn(matchesSubcollection);
        ApiFuture<QuerySnapshot> matchesFuture = mock(ApiFuture.class);
        when(matchesSubcollection.get()).thenReturn(matchesFuture);
        
        // Create mock match documents
        QuerySnapshot matchesSnapshot = mock(QuerySnapshot.class);
        when(matchesFuture.get()).thenReturn(matchesSnapshot);
        QueryDocumentSnapshot matchDoc1 = mock(QueryDocumentSnapshot.class);
        when(matchesSnapshot.getDocuments()).thenReturn(Arrays.asList(matchDoc1));
        when(matchDoc1.getId()).thenReturn("Match1");
        when(matchDoc1.getReference()).thenReturn(mock(DocumentReference.class));
        
        // Mock delete on match document
        when(matchDoc1.getReference().delete()).thenReturn(mock(ApiFuture.class));
        
        // Mock standings subcollection
        CollectionReference standingsSubcollection = mock(CollectionReference.class);
        when(roundDocument.collection("standing")).thenReturn(standingsSubcollection);
        ApiFuture<QuerySnapshot> standingsFuture = mock(ApiFuture.class);
        when(standingsSubcollection.get()).thenReturn(standingsFuture);
        
        // Create mock standing documents
        QuerySnapshot standingsSnapshot = mock(QuerySnapshot.class);
        when(standingsFuture.get()).thenReturn(standingsSnapshot);
        QueryDocumentSnapshot standingDoc1 = mock(QueryDocumentSnapshot.class);
        when(standingsSnapshot.getDocuments()).thenReturn(Arrays.asList(standingDoc1));
        when(standingDoc1.getId()).thenReturn("Standing1");
        when(standingDoc1.getReference()).thenReturn(mock(DocumentReference.class));
        
        // Mock delete on standing document
        when(standingDoc1.getReference().delete()).thenReturn(mock(ApiFuture.class));

        // Mock delete on round document
        when(roundDocument.delete()).thenReturn(apiFutureWriteResult);
        Timestamp mockTimestamp = mock(Timestamp.class);
        when(mockTimestamp.toString()).thenReturn("mockTime");
        when(apiFutureWriteResult.get()).thenReturn(mock(WriteResult.class));
        when(apiFutureWriteResult.get().getUpdateTime()).thenReturn(mockTimestamp);
        
        // Act
        String result = tournamentService.deleteRound(tournamentName, roundName);

        // Assert
        assertEquals("Deleted round: Round1, time: mockTime", result);
        
        // Verify calls
        verify(matchDoc1.getReference()).delete();
        verify(standingDoc1.getReference()).delete();
        verify(roundDocument, times(1)).delete();
    }

    @Test
    void deleteRound_WithNoMatchesAndStandings_ReturnsSuccessMessage() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "SampleTournament";
        String roundName = "Round1";

        // Mock the Firestore structure
        when(firestore.collection("tournament")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("tournament").document(tournamentName)).thenReturn(tournamentDocument);
        when(tournamentDocument.collection("round")).thenReturn(mock(CollectionReference.class));
        when(tournamentDocument.collection("round").document(roundName)).thenReturn(roundDocument);

        // Mock matches subcollection
        CollectionReference matchesSubcollection = mock(CollectionReference.class);
        when(roundDocument.collection("match")).thenReturn(matchesSubcollection);
        ApiFuture<QuerySnapshot> matchesFuture = mock(ApiFuture.class);
        when(matchesSubcollection.get()).thenReturn(matchesFuture);
        QuerySnapshot matchesSnapshot = mock(QuerySnapshot.class);
        when(matchesFuture.get()).thenReturn(matchesSnapshot);
        when(matchesSnapshot.getDocuments()).thenReturn(Collections.emptyList()); // No matches

        // Mock standings subcollection
        CollectionReference standingsSubcollection = mock(CollectionReference.class);
        when(roundDocument.collection("standing")).thenReturn(standingsSubcollection);
        ApiFuture<QuerySnapshot> standingsFuture = mock(ApiFuture.class);
        when(standingsSubcollection.get()).thenReturn(standingsFuture);
        QuerySnapshot standingsSnapshot = mock(QuerySnapshot.class);
        when(standingsFuture.get()).thenReturn(standingsSnapshot);
        when(standingsSnapshot.getDocuments()).thenReturn(Collections.emptyList()); // No standings

        // Mock delete on round document
        when(roundDocument.delete()).thenReturn(apiFutureWriteResult);
        Timestamp mockTimestamp = mock(Timestamp.class);
        when(mockTimestamp.toString()).thenReturn("mockTime");
        when(apiFutureWriteResult.get()).thenReturn(mock(WriteResult.class));
        when(apiFutureWriteResult.get().getUpdateTime()).thenReturn(mockTimestamp);
        
        // Act
        String result = tournamentService.deleteRound(tournamentName, roundName);

        // Assert
        assertEquals("Deleted round: Round1, time: mockTime", result);
        
        // Verify round document deletion
        verify(roundDocument).delete();
    }

    @Test
    void roundEnd_WithExistingTournament_NotLastRound_UpdatesRoundAndReturnsSuccessMessage() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "SampleTournament";
        String roundName = "Round1";
        Round round = new Round(roundName, Collections.emptyList());


        Tournament tournament = new Tournament();
        tournament.setCurrentRound("1");  // Setting initial round to 1

        // Mock the getTournament method to return the mock tournament
        doReturn(true).when(tournamentService).isTournamentInProgress(tournamentName);
        doReturn(tournament).when(tournamentService).getTournament(tournamentName);
        doReturn(round).when(tournamentService).getRound(tournamentName, roundName);
        doReturn("Tournament Updated").when(tournamentService).updateTournament(tournament);
        // Mock the updateRound method
        doReturn("Correct String").when(tournamentService).updateRound(anyString(), any(Round.class));

        doReturn(false).when(tournamentService).isLastRound(tournamentName);

        // Act
        String result = tournamentService.roundEnd(tournamentName, roundName);

        // Assert
        assertEquals("Round Number Updated", result);
        assertEquals("2", tournament.getCurrentRound()); // Check if the round number was updated correctly
        verify(tournamentService).updateTournament(tournament); // Verify updateTournament was called
    }

    @Test
    void roundEnd_WithNonExistingTournament_ReturnsNotFoundMessage() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentName = "NonExistingTournament";
        String roundName = "Round1";

        doReturn(true).when(tournamentService).isTournamentInProgress(tournamentName);

        // Mock the getTournament method to return null
        doReturn(null).when(tournamentService).getTournament(tournamentName);
        
        // Act
        String result = tournamentService.roundEnd(tournamentName, roundName);

        // Assert
        assertEquals("Tournament not found", result);
        verify(tournamentService, never()).updateTournament(any()); // Ensure updateTournament was never called
    }

    @Test
    void generateMatchId_ValidInputs_ReturnsCorrectMatchId() {
        // Arrange
        String tournamentName = "TestTournament";
        String roundName = "Round 1";
        String player1 = "Player One";
        String player2 = "Player Two";
        
        // Act
        String matchId = tournamentService.generateMatchId(tournamentName, roundName, player1, player2);
        
        // Assert
        String expectedMatchId = "TestTournament_Round-1_Player-One_Player-Two";
        assertEquals(expectedMatchId, matchId);
    }

    @Test
    void generateMatchId_InputsWithExtraSpaces_ReturnsTrimmedMatchId() {
        // Arrange
        String tournamentName = "  TestTournament  ";
        String roundName = "  Round 1  ";
        String player1 = " Player One ";
        String player2 = " Player Two ";
        
        // Act
        String matchId = tournamentService.generateMatchId(tournamentName, roundName, player1, player2);
        
        // Assert
        String expectedMatchId = "TestTournament_Round-1_Player-One_Player-Two";
        assertEquals(expectedMatchId, matchId);
    }

    @Test
    void generateMatchId_EmptyStrings_ReturnsUnderscoreSeparatedMatchId() {
        // Arrange
        String tournamentName = "";
        String roundName = "";
        String player1 = "";
        String player2 = "";
        
        // Act
        String matchId = tournamentService.generateMatchId(tournamentName, roundName, player1, player2);
        
        // Assert
        String expectedMatchId = "___";
        assertEquals(expectedMatchId, matchId);
    }

    @Test
    void startTournament_WhenTournamentNotInProgress_StartsTournament() throws ExecutionException, InterruptedException {
        String tournamentName = "TestTournament";

        // Arrange - Set up behavior of helper methods using doReturn
        doReturn(false).when(tournamentService).isTournamentInProgress(tournamentName);
        doReturn(validTournament).when(tournamentService).getTournament(tournamentName);

        // Mocking the creation and updating processes
        AlgoRound mockAlgoRound = mock(AlgoRound.class);
        List<Match> mockMatches = List.of(new Match(), new Match());

        // Stubbing internal method calls
        doReturn(mockAlgoRound).when(tournamentService).parseIntoAlgoRound1(validTournament);
        doReturn(mockMatches).when(tournamentService).parseAlgoRoundMatchesToMatch(mockAlgoRound);

        doReturn("Correct String").when(tournamentService).createRound(eq(tournamentName), any(Round.class));
        doNothing().when(tournamentService).updateDataBaseWithMatches(eq(validTournament), eq("1"), eq(mockMatches));
        doReturn("Correct String").when(tournamentService).updateTournament(validTournament);

        // Act
        boolean result = tournamentService.startTournament(tournamentName);

        // Assert
        assertTrue(result);
        assertTrue(validTournament.isInProgress());
        assertEquals("1", validTournament.getCurrentRound());
        
        // Verify calls
        verify(tournamentService).createRound(eq(tournamentName), any(Round.class));
        verify(tournamentService).updateDataBaseWithMatches(eq(validTournament), eq("1"), eq(mockMatches));
        verify(tournamentService).updateTournament(validTournament);
        verify(mockAlgoRound).generateRoundOne();
    }


}
