package com.java.firebase.demo.tournament;

import com.java.firebase.demo.user.User;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter

public class Tournament {
    private String tournamentName; // also used as documentId
    private String startDate; 
    private String endDate;
    private String registrationDeadline;
    private int numberOfPlayers;
    private String tournamentDesc;
    private String location;
    private String imageUrl;
    // private String inProgress;
    
    // List of participating players (e.g., could be just player IDs, or Player objects)
    // i think this isnt how were supposed to do it
    private List<String> adminList;
    private List<String> participatingPlayers;
    private List<Round> rounds;
    
}
