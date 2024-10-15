import java.util.Comparator;

import com.java.firebase.demo.tournament.TournamentPlayer;

public class playerComparator implements Comparator<TournamentPlayer> {

    public int compare(TournamentPlayer p1, TournamentPlayer p2) {

        if (p1.getTotalMatchPoints() > p2.getTotalMatchPoints()) {
            return 1;
        }

        if (p1.getTotalMatchPoints() < p2.getTotalMatchPoints()) {
            return -1;
        }

        if (p1.getOMW() > p2.getOMW()) {
            return 1;
        }

        if (p1.getOMW() < p2.getOMW()) {
            return -1;
        }

        if (p1.getCurOGW() > p2.getCurOGW()) {
            return 1;
        }

        if (p1.getCurOGW() < p2.getCurOGW()) {
            return -1;
        }

        if (p1.getCurGW() > p2.getCurGW()) {
            return 1;
        }

        if (p1.getCurGW() < p2.getCurGW()) {
            return -1;
        }

        // If all comparisons are equal, return 0 (they are equal)
        return 0;
    }
}
