package com.java.firebase.demo.tournament;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter

public class Round {
    private String roundName;
    private List<Match> matches;
    private List<Standing> standings;
}
