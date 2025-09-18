package com.rush.doodle.player;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/players")
public class PlayerController {
	@Autowired
	private PlayerService playerService;
	@GetMapping("/room/{roomId}")
	public List<Player> getPlayersInRoom(@PathVariable String roomId){
		return playerService.getPlayersInRoom(roomId);
	}
	@PostMapping("/join")
	@CrossOrigin(origins = "http://localhost:5173" )
	public ResponseEntity<?> addPlayer(@Valid @RequestBody PlayerJoinRequestDTO player) {
		System.out.println("Incoming DTO -> name: '" + player.getPlayerName() + "', roomId: '" + player.getRoomId() + "'");
		return playerService.addPlayer(player);
	}
	@DeleteMapping("/{name}")
	public void deletePlayer(@PathVariable String name) {
		 playerService.deletePlayer(name);
	}
}
