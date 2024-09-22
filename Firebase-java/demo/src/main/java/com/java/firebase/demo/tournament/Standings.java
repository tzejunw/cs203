package com.java.firebase.demo.tournament;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class Standings {
    private String standingsId;
    private String name;
    private String matchRecord;
    private long omw;
    private int points;
    private int rank;
    private long gw;
    
}
