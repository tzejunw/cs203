package com.java.firebase.demo.tournament;

import com.java.firebase.demo.user.User;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter

public class Tournament {
    private String tournamentName;
    private String startDate;
    private String endDate;
    private int numberOfPlayers;
    
    // List of participating players (e.g., could be just player IDs, or Player objects)
    // i think this isnt how were supposed to do it
    private List<User> participatingPlayers;
    private List<Round> rounds;
    
}
