package com.java.firebase.demo.tournament;
import java.util.*;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipatingPlayer {
    public String userName;
    public List<String> pastMatches;
}
