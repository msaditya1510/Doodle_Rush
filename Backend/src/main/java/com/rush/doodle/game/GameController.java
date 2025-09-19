package com.rush.doodle.game;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rush.doodle.game.core.GameFlowService;
import com.rush.doodle.game.leaderboard.LeaderboardService;
import com.rush.doodle.player.PlayerDTO;

@RestController
@RequestMapping("/game")
public class GameController {
	@Autowired
	private GameService gameService;
//	@Autowired private RoundService roundService;
	@Autowired private GameFlowService gameFlowService;
	@Autowired private LeaderboardService leaderboardService;
	
	
	@PostMapping("/startGame/{roomId}/{playerName}")
	public ResponseEntity<?> startGame(@PathVariable String roomId,@PathVariable String playerName) {
		return gameService.startGame(roomId,playerName);
	}
	
	@GetMapping("/end/{roomId}")
	public String endGame(@PathVariable String roomId) {
		return gameFlowService.nextRoundOrEndGame(roomId);
	}
	@PostMapping("/startRound/{roomId}/{playerName}")
	public String startRound(@PathVariable String roomId,@PathVariable String playerName) {
		return gameFlowService.startRound(roomId,playerName);
	}
	@PostMapping("/{roomId}/guess")
    public String submitGuess(@PathVariable String roomId,@RequestParam Long playerId,@RequestParam String guess) throws Exception {
		try {
        return gameService.submitGuess(roomId, playerId, guess);
        }catch(Exception e) {return "";}
    }
	@GetMapping("/leaderboard/{roomId}")
    public List<PlayerDTO> getLeaderboard(@PathVariable String roomId) {
        return leaderboardService.getLeaderboard(roomId);
    }
}
