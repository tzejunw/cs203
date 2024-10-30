package com.java.firebase.demo.tournament;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Standing { // a standing is a tournament Player, minus some attributes
    private int rank;
    private String playerID;
    private int curMatchPts;
    private double curOMW;
    private int curGamePts;
    private double curOGW;
}
