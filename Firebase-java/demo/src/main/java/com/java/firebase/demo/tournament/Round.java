package com.java.firebase.demo.tournament;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Round {

    private String roundName;
    private List<Match> matches;
    private List<Standing> standings;
    private boolean isOver;

    public Round(String roundName) {
        this.roundName = roundName;

    }

    public Round(String roundName, List<Match> matches) {
        this.roundName = roundName;
        this.matches = matches;
    }
}
