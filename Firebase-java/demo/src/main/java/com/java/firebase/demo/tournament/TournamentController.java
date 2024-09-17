package com.java.firebase.demo.tournament;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ExecutionException;

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

    @GetMapping("/tournament/get") // documentId is the user's tournamentName. The argument here determines what it expects as the key in Postman
    public Tournament getTournament(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return tournamentService.getTournament(documentId);
    }

    @PutMapping("/tournament/update") // takes another tournament json
    public String updateTournament(@RequestBody Tournament tournament) throws InterruptedException, ExecutionException {
        return tournamentService.updateTournament(tournament);
    }

    @DeleteMapping("/tournament/delete") // documentId is the user's tournamentName. The argument here determines what it expects as the key in Postman
    public String deleteTournament(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return tournamentService.deleteTournament(documentId);
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
    public Round getRound(@RequestParam String tournamentName, @RequestParam String roundName) throws InterruptedException, ExecutionException {
        return tournamentService.getRound(tournamentName, roundName);
    }
    // no put mapping for rounds, no fields
    @DeleteMapping("/tournament/round/delete")
    public String deleteRound(@RequestParam String tournamentName, @RequestParam String roundName) throws InterruptedException, ExecutionException {
        return tournamentService.deleteRound(tournamentName, roundName);
}

    @PostMapping("/tournament/round/match/create")
    public String createMatch(@RequestParam String tournamentName, @RequestParam String roundName, @RequestBody Match match) throws InterruptedException, ExecutionException{
        return tournamentService.createMatch(tournamentName, roundName, match);
    }

    @GetMapping("/tournament/round/match/get") // The argument here determines what it expects as the key in Postman
    public Match getMatch(@RequestParam String tournamentName, @RequestParam String roundName, @RequestParam String player1, @RequestParam String player2) throws InterruptedException, ExecutionException {
        return tournamentService.getMatch(tournamentName, roundName, player1, player2);
    }

    @PutMapping("/tournament/round/match/update")
    public String updateMatch(@RequestParam String tournamentName, 
                              @RequestParam String roundName, 
                              @RequestParam String player1, 
                              @RequestParam String player2, 
                              @RequestBody Match match) throws InterruptedException, ExecutionException {
        return tournamentService.updateMatch(tournamentName, roundName, player1, player2, match);
    }
        
    @DeleteMapping("/tournament/round/match/delete")
    public String deleteMatch(@RequestParam String tournamentName, 
                              @RequestParam String roundName, 
                              @RequestParam String player1, 
                              @RequestParam String player2) throws InterruptedException, ExecutionException {
        return tournamentService.deleteMatch(tournamentName, roundName, player1, player2);
    }
    
    @PostMapping("/tournament/round/standing/create")
    public String createStanding(@RequestParam String tournamentName, @RequestParam String roundName, @RequestBody Standing standing) throws InterruptedException, ExecutionException {
        return tournamentService.createStanding(tournamentName, roundName, standing);
    }
    
    @GetMapping("/tournament/round/standing/get")
    public Standing getStanding(@RequestParam String tournamentName, @RequestParam String roundName, @RequestParam int rank) throws InterruptedException, ExecutionException {
        return tournamentService.getStanding(tournamentName, roundName, rank);
    }
    
    @PutMapping("/tournament/round/standing/update")
    public String updateStanding(@RequestParam String tournamentName, @RequestParam String roundName, @RequestBody Standing standing) throws InterruptedException, ExecutionException {
        return tournamentService.updateStanding(tournamentName, roundName, standing);
    }
    
    @DeleteMapping("/tournament/round/standing/delete")
    public String deleteStanding(@RequestParam String tournamentName, @RequestParam String roundName, @RequestParam int rank) throws InterruptedException, ExecutionException {
        return tournamentService.deleteStanding(tournamentName, roundName, rank);
    }
}