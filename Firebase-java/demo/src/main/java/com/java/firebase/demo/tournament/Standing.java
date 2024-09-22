package com.java.firebase.demo.tournament;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class Standing {
    private int rank;
    private String name;
    private String matchRecord;
    private long omw;
    private int points;
    private long gw;
}
