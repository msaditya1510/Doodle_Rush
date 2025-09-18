package com.rush.doodle.player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.rush.doodle.WebSocket.ChatMessage;
import com.rush.doodle.WebSocket.ChatType;
import com.rush.doodle.exceptions.*;
import com.rush.doodle.room.*;

@Service
public class PlayerService {
	@Autowired 
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private PlayerRepository playerRepository;
	@Autowired
	private RoomRepository roomRepository;
	
	public List<Player> getPlayersInRoom(String roomId) {
		List<Player> players=new ArrayList<>();
		Optional<Room> room=roomRepository.findByRoomId(roomId);
		if(room.isPresent()) {
			room.get().getPlayers().forEach(players::add);
			return players;
		}
		throw new IllegalStateException("Room Not Found!");
	}

	public ResponseEntity<?> addPlayer(PlayerJoinRequestDTO request) {
		ChatMessage chatMessage=new ChatMessage();
		
		String name=request.getPlayerName();
		String roomId=request.getRoomId();
		Room room=roomRepository.findByRoomId(roomId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found for: " + roomId));
		
		// check if the room's game started or not.
		if (room.isGameActive()) {
		    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot join, game already started!");
		}
		List<Player> players=room.getPlayers() != null ? room.getPlayers() : new ArrayList<>();
		boolean exists=players.stream().anyMatch(p->p.getName().equalsIgnoreCase(name));
		if(exists) {
			throw new DuplicateException("Duplicate name is found, try another one!");
		}
		//creating new player
		Player player=new Player();
		player.setName(name);
		player.setRoom(room);
		room.addPlayer(player);
		player.setLastActive(LocalDateTime.now());
		
		//checking and setting the host
		if(room.getHost()==null) {
			room.setHost(player);
		}
		
		playerRepository.save(player);
		roomRepository.save(room);
		
		//next 3 lines are for chatMessage
		chatMessage.setName(name);
		chatMessage.setType(ChatType.JOIN);
		chatMessage.setContent(name+" joined the room!");
		messagingTemplate.convertAndSend("/topic/room/"+roomId, chatMessage);
		if (room.getHost().equals(player)) {
		    return ResponseEntity.status(HttpStatus.OK)
		            .body(player.getName() + " joined as HOST!");
		} else {
		    return ResponseEntity.status(HttpStatus.OK)
		            .body(player.getName() + " joined successfully!");
		}
	}

	public void deletePlayer(String name) {
	    Optional<Player> playerOpt = playerRepository.findByName(name);

	    if (playerOpt.isPresent()) {
	        Player player = playerOpt.get();
	        Room room = player.getRoom();

	        // Remove player from room
	        room.removePlayer(player);
	        playerRepository.deleteById(player.getPlayerId());
	        ChatMessage message = new ChatMessage();

	        // Handle host reassignment
	        if (room.getHost().equals(player)) {
	            if (!room.getPlayers().isEmpty()) {
	                Player newHost = room.getPlayers().get(0); // pick first
	                room.setHost(newHost);

	                message.setAll("Player " + player.getName() + " removed due to inactivity. " +newHost.getName() + " is now the host.",
	                    "System",ChatType.SYSTEM);
	                messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), message);
	            }
	         else {
	                // No players left → delete room
	                roomRepository.delete(room);
	                return;
	            }
	        } 
	        else {
	            // Normal removal (not host)
	            message.setAll("Player " + player.getName() + " removed due to inactivity!","System",ChatType.SYSTEM);
	            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), message);
	        }
	        roomRepository.save(room); // save changes (new host or updated players)
	    }
	}

}
