package com.java.firebase.demo.tournament;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class Match {
    private String player1;
    private String player2;
    private String winner;
    private int wins;
    private int losses;
    private boolean isDraw;
    private boolean isBye;
}
