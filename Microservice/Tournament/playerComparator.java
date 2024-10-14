import java.util.Comparator;


public class playerComparator implements Comparator<TournamentPlayer>{

    public int compare( TournamentPlayer p1, TournamentPlayer p2){

        // if (p1.getTotalMatchPoints() == p2.getTotalMatchPoints()){
        //     if (p1.getOMW() == p2.getOMW()){
        //         if (p1.getCurGW() == p2.getCurGW()){
        //             return p1.getOGW() > p2.getOGW() ? 1 : -1;   // might need random
        //         }else{
        //             return p1.getCurGW() > p2.getCurGW() ? 1 : -1;
        //         }

        //     }else{
        //         return p1.getOMW() > p2.getOMW() ? 1 : -1;
        //     }

        // }else{
        //     return p1.getTotalMatchPoints() - p2.getTotalMatchPoints();
        // }

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

        return -p1.getUserID().compareTo(p2.getUserID());
        //return 0;
    }
}

