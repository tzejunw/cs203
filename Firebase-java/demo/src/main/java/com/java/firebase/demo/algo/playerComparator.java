package com.java.firebase.demo.algo;

import java.util.Comparator;


public class playerComparator implements Comparator<AlgoTournamentPlayer>{

    public int compare( AlgoTournamentPlayer p1, AlgoTournamentPlayer p2){


        if (p1.getTotalMatchPoints() > p2.getTotalMatchPoints()){
            return 1;
        }

        if (p1.getTotalMatchPoints() < p2.getTotalMatchPoints()){
            return -1;
        }

        if (p1.getOMW()> p2.getOMW()){
            return 1;
        }

        if (p1.getOMW() < p2.getOMW()){
            return -1;
        }

        if (p1.getCurOGW() > p2.getCurOGW()){
            return 1;
        }

        if (p1.getCurOGW() < p2.getCurOGW()){
            return -1;
        }

        if (p1.getCurGW() > p2.getCurGW()){
            return 1;
        }
        if (p1.getCurGW() < p2.getCurGW()){
            return -1;
        }

        return -p1.getPlayerID().compareTo(p2.getPlayerID());

    }
}

