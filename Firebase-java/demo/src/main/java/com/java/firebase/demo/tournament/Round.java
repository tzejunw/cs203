package com.java.firebase.demo.tournament;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter

public class Round {
    private String roundId;
    private List<Match> matches;
    private List<Standings> standings;
}
