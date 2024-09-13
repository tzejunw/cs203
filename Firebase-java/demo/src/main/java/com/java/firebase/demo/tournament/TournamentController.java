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

    @GetMapping("/tournament/get") 
    public Tournament getTournament(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return tournamentService.getTournament(documentId);
    }

    @PutMapping("/tournament/update") // takes another tournament json
    public String updateTournament(@RequestBody Tournament tournament) throws InterruptedException, ExecutionException {
        return tournamentService.updateTournament(tournament);
    }

    @DeleteMapping("/tournament/delete")
    public String deleteTournament(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return tournamentService.deleteTournament(documentId);
    }

    @GetMapping("/tournament/test")
    public ResponseEntity<String> testGetEndpoint(){
        return ResponseEntity.ok("Test Get Endpoint is Working");
    }



    // // Rounds
    // @PostMapping("/{tournamentId}/rounds")
    // public void addRound(@PathVariable String tournamentId, @RequestBody Round round) {
    //     tournamentService.addRound(tournamentId, round);
    // }

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
