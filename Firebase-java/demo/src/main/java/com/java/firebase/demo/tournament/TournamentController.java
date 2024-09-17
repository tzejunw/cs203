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

    // updateMatch
    // delete match

    // CRUD STANDINGS


    // @GetMapping("/{tournamentId}/rounds")
    // public List<Round> getRounds(@PathVariable String tournamentId) throws Exception {
    //     return tournamentService.getRounds(tournamentId);
    // }

    // // Matches
    // @PostMapping("/{tournamentId}/rounds/{roundId}/matches")
    // public void addMatch(@PathVariable String tournamentId, @PathVariable String roundId, @RequestBody Match match) {
    //     tournamentService.addMatch(tournamentId, roundId, match);
    // }

    // @GetMapping("/{tournamentId}/rounds/{roundId}/matches")
    // public List<Match> getMatches(@PathVariable String tournamentId, @PathVariable String roundId) throws Exception {
    //     return tournamentService.getMatches(tournamentId, roundId);
    // }

    // // Standings
    // @PostMapping("/{tournamentId}/rounds/{roundId}/standings")
    // public void addStanding(@PathVariable String tournamentId, @PathVariable String roundId, @RequestBody Standings standing) {
    //     tournamentService.addStanding(tournamentId, roundId, standing);
    // }

    // @GetMapping("/{tournamentId}/rounds/{roundId}/standings")
    // public List<Standings> getStandings(@PathVariable String tournamentId, @PathVariable String roundId) throws Exception {
    //     return tournamentService.getStandings(tournamentId, roundId);
    // }
}
