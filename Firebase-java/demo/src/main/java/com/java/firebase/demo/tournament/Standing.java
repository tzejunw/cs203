package com.java.firebase.demo.tournament;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class Standing {
    private int rank;
    private String name;
    private String matchRecord;
    private int points;
    private double omw;
    private double gw;
    private double ogw;
}
