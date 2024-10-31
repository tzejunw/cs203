package com.java.firebase.demo.tournament;

import com.java.firebase.demo.user.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    private String tournamentName; // also used as documentId
    private String startDate; 
    private String endDate;
    private String registrationDeadline;
    private int numberOfPlayers;
    private String tournamentDesc;
    private String location;
    private String imageUrl;
    private boolean inProgress;
    private String currentRound;
    private int expectedNumRounds;
    
    // List of participating players (e.g., could be just player IDs, or Player objects)
    // i think this isnt how were supposed to do it
    private List<String> adminList;
    private List<String> participatingPlayers;
    private List<Round> rounds;

    
    
}
