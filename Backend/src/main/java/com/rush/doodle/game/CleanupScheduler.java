package com.rush.doodle.game;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rush.doodle.player.Player;
import com.rush.doodle.player.PlayerRepository;
import com.rush.doodle.player.PlayerService;
import com.rush.doodle.room.Room;
import com.rush.doodle.room.RoomRepository;

@Component
public class CleanupScheduler {
	private PlayerService playerService;
	private  PlayerRepository playerRepository;
	private RoomRepository roomRepository;

	public CleanupScheduler(PlayerService playerService, 
		 PlayerRepository playerRepository,
	     RoomRepository roomRepository) {
		
		 this.playerService = playerService;
		 this.playerRepository = playerRepository;
		 this.roomRepository = roomRepository;
		  System.out.println(">>> CleanupScheduler bean created by Spring!");
}
	public PlayerRepository getPlayerRepository() {
		return playerRepository;
	}
	public void setPlayerRepository(PlayerRepository playerRepository) {
		this.playerRepository = playerRepository;
	}
	public RoomRepository getRoomRepository() {
		return roomRepository;
	}
	public void setRoomRepository(RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}
	@Scheduled(fixedRate=60000) //a scheduler will run after every 1 minute to check for inactive objects
	public void cleanup() {
		LocalDateTime cutoff=LocalDateTime.now().minusMinutes(5);
		//for inactive players: if he is inactive for 5 minutes he will be removed from the room
		List<Player> inactivePlayers=playerRepository.findByLastActiveBefore(cutoff);
		if(!inactivePlayers.isEmpty()) {
			inactivePlayers.forEach(p->playerService.deletePlayer(p.getName()));
		}
	//for clearing inactive rooms in every 10 minutes
	LocalDateTime roomCutoff=LocalDateTime.now().minusMinutes(10);
	List<Room> inactiveRooms=roomRepository.findByLastActiveBefore(roomCutoff);
	if(!inactiveRooms.isEmpty()){
		roomRepository.deleteAll(inactiveRooms);
		System.out.println("Inactive rooms for the last 10 minutes are now deleted!");
	}
	}
}
