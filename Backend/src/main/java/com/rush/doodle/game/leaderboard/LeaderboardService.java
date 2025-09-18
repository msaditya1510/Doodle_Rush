package com.rush.doodle.game.leaderboard;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rush.doodle.exceptions.NotFoundException;
import com.rush.doodle.player.Player;
import com.rush.doodle.player.PlayerDTO;
import com.rush.doodle.player.PlayerRepository;
import com.rush.doodle.room.Room;
import com.rush.doodle.room.RoomRepository;

@Service
public class LeaderboardService {

	@Autowired RoomRepository roomRepository;
	@Autowired PlayerRepository playerRepository;
	
	public List<PlayerDTO> getLeaderboard(String roomId) {
		Room room=roomRepository.findByRoomId(roomId).orElseThrow(()-> new NotFoundException("Room not found: " + roomId));
		List<Player> players=playerRepository.findByRoom(room);
		return players.stream()
		        .sorted((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore())) // sort desc by score
		        .map(p -> new PlayerDTO(p.getPlayerId(), p.getName(), p.getScore())) // only return needed fields
		        .collect(Collectors.toList());
	}	
}
