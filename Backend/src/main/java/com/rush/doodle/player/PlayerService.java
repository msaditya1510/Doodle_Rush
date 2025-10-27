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

import jakarta.transaction.Transactional;

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
		messagingTemplate.convertAndSend("/topic/room/"+room.getRoomId(), chatMessage);
		if (room.getHost().equals(player)) {
		    return ResponseEntity.status(HttpStatus.OK)
		            .body(player.getName() + " joined as HOST!");
		} else {
		    return ResponseEntity.status(HttpStatus.OK)
		            .body(player.getName() + " joined successfully!");
		}
	}

	@Transactional
	public void deletePlayer(String name) {
	    Optional<Player> playerOpt = playerRepository.findByName(name);

	    if (playerOpt.isPresent()) {
	        Player player = playerOpt.get();
	        Room room = player.getRoom();
	        ChatMessage message = new ChatMessage();

	        // Check if player is host BEFORE deleting
	        boolean wasHost = room.getHost() != null && room.getHost().equals(player);

	        // Remove player from room first
	        room.removePlayer(player);

	        // Handle host reassignment BEFORE deleting player
	        if (wasHost) {
	            if (!room.getPlayers().isEmpty()) {
	                Player newHost = room.getPlayers().get(0); // pick first
	                room.setHost(newHost);

	                message.setAll("Player " + player.getName() + " left the game. " + newHost.getName() + " is now the host.",
	                    "System", ChatType.SYSTEM);
	            } else {
	                // No players left → delete room
	                roomRepository.delete(room);
	                playerRepository.deleteById(player.getPlayerId()); // Delete player after room decision
	                return;
	            }
	        } else {
	            // Normal removal (not host)
	            message.setAll("Player " + player.getName() + " left the game", "System", ChatType.LEAVE);
	        }

	        // Send the message
	        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), message);

	        // Save room changes
	        roomRepository.save(room);

	        // Delete player LAST
	        playerRepository.deleteById(player.getPlayerId());
	    }
	}
}
