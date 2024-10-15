package com.java.firebase.demo.tournament;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping("/tournament/create")
    public String createTournament(@RequestBody Tournament tournament) throws InterruptedException, ExecutionException {
        return tournamentService.createTournament(tournament);
    }

    // @PostMapping("/tournament/start") // to be implemeted
    // public String startTournament(@RequestParam String tournamentName) throws InterruptedException, ExecutionException {
    //     return tournamentService.startTournament(tournamentName);
    // }

    // /tournamnet/end
    // update inProgress field to false
    // this will only happen afeter stadnigns are updated. 

    @GetMapping("/tournament/get") // documentId is the user's tournamentName. The argument here determines what it expects as the key in Postman
    public Tournament getTournament(@RequestParam String tournamentName) throws InterruptedException, ExecutionException {
        return tournamentService.getTournament(tournamentName);
    }

    @GetMapping("/tournament/get/all") // Adjust the route path as needed
    public List<Tournament> getAllTournaments() throws InterruptedException, ExecutionException {
        return tournamentService.getAllTournaments();
    }


    @PutMapping("/tournament/update") // takes another tournament json. DO NOT TRY TO give it an incomplete json. FOLLOW THE VALIDATION RULES
    public String updateTournament(@RequestBody Tournament tournament) throws InterruptedException, ExecutionException {
        return tournamentService.updateTournament(tournament);
    }


    @DeleteMapping("/tournament/delete") // documentId is the user's tournamentName. The argument here determines what it expects as the key in Postman
    public String deleteTournament(@RequestParam String tournamentName) throws InterruptedException, ExecutionException {
        return tournamentService.deleteTournament(tournamentName);
    }

    @GetMapping("/tournament/test")
    public ResponseEntity<String> testGetEndpoint(){
        return ResponseEntity.ok("Test Get Endpoint is Working");
    }

    // // Rounds
    @PostMapping("/tournament/round/create")
    public String createRound(@RequestParam String tournamentName, @RequestBody Round round) throws InterruptedException, ExecutionException{
        return tournamentService.createRound(tournamentName, round);
    }

    @GetMapping("/tournament/round/get") // The argument here determines what it expects as the key in Postman
    public Round getRound(@RequestParam String tournamentName, @RequestParam int roundNumber) throws InterruptedException, ExecutionException {
        return tournamentService.getRound(tournamentName, roundNumber);
    }
    // no put mapping for rounds, no fields
    @DeleteMapping("/tournament/round/delete")
    public String deleteRound(@RequestParam String tournamentName, @RequestParam int roundNumber) throws InterruptedException, ExecutionException {
        return tournamentService.deleteRound(tournamentName, roundNumber);

    //TODO
    // /tournament/round/end 
    // get all matches which have been updated with score (finished) this will be json returned. 
    // convert the json to objects in java!! (hard)

    // calls algo.roundend(match objects). feed match objects into algo function.
    // algo function returns all standings as objects (each object is one record)
    // commit those record to firebase!.
    // say good

    // /tournament/round/startnextround
    // give participatingtournamentplayers java objects (need list of prev matches) and standings java objects to algo
    // sometingliek algo.startnextround(everything above)
    // it will return next round matches, java object. convert that to json.
    // commmit that to firebase. 
    // say ok

}

    @PostMapping("/tournament/round/match/create")
    public String createMatch(@RequestParam String tournamentName, @RequestParam int roundNumber, @RequestBody Match match) throws InterruptedException, ExecutionException{
        return tournamentService.createMatch(tournamentName, roundNumber, match);
    }

    @GetMapping("/tournament/round/match/get") // The argument here determines what it expects as the key in Postman
    public Match getMatch(@RequestParam String tournamentName, @RequestParam int roundNumber, @RequestParam String player1, @RequestParam String player2) throws InterruptedException, ExecutionException {
        return tournamentService.getMatch(tournamentName, roundNumber, player1, player2);
    }

    @PutMapping("/tournament/round/match/update")
    public String updateMatch(@RequestParam String tournamentName, 
                              @RequestParam int roundNumber, 
                              @RequestParam String player1, 
                              @RequestParam String player2, 
                              @RequestBody Match match) throws InterruptedException, ExecutionException {
        return tournamentService.updateMatch(tournamentName, roundNumber, player1, player2, match);
    }
        
    @DeleteMapping("/tournament/round/match/delete")
    public String deleteMatch(@RequestParam String tournamentName, 
                              @RequestParam int roundNumber, 
                              @RequestParam String player1, 
                              @RequestParam String player2) throws InterruptedException, ExecutionException {
        return tournamentService.deleteMatch(tournamentName, roundNumber, player1, player2);
    }
    
    @PostMapping("/tournament/round/standing/create")
    public String createStanding(@RequestParam String tournamentName, @RequestParam int roundNumber, @RequestBody Standing standing) throws InterruptedException, ExecutionException {
        return tournamentService.createStanding(tournamentName, roundNumber, standing);
    }
    
    @GetMapping("/tournament/round/standing/get")
    public Standing getStanding(@RequestParam String tournamentName, @RequestParam int roundNumber, @RequestParam int rank) throws InterruptedException, ExecutionException {
        return tournamentService.getStanding(tournamentName, roundNumber, rank);
    }
    
    @PutMapping("/tournament/round/standing/update")
    public String updateStanding(@RequestParam String tournamentName, @RequestParam int roundNumber, @RequestBody Standing standing) throws InterruptedException, ExecutionException {
        return tournamentService.updateStanding(tournamentName, roundNumber, standing);
    }
    
    @DeleteMapping("/tournament/round/standing/delete")
    public String deleteStanding(@RequestParam String tournamentName, @RequestParam int roundNumber, @RequestParam int rank) throws InterruptedException, ExecutionException {
        return tournamentService.deleteStanding(tournamentName, roundNumber, rank);
    }

    
}