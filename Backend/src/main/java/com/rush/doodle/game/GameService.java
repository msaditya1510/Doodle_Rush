package com.rush.doodle.game;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.rush.doodle.WebSocket.ChatMessage;
import com.rush.doodle.WebSocket.ChatType;
import com.rush.doodle.exceptions.NotFoundException;
import com.rush.doodle.game.core.GameFlowService;
import com.rush.doodle.game.round.RoundService;
import com.rush.doodle.player.Player;
import com.rush.doodle.player.PlayerRepository;
import com.rush.doodle.room.Room;
import com.rush.doodle.room.RoomRepository;

import jakarta.transaction.Transactional;
@Service
public class GameService {
	@Autowired
	private SimpMessagingTemplate messagingTemplate; //to broadcast the messages
	@Autowired
	private RoomRepository roomRepository;
	@Autowired
	private PlayerRepository playerRepository;
	@Autowired private RoundService roundService;
	@Autowired private GameFlowService gameFlowService;
	
	private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
	//Temporary words
		
	// to start the game only host can do this
	public ResponseEntity<String> startGame(String roomId,String hostName) {
		ChatMessage chatMessage=new ChatMessage();
		Room room=roomRepository.findByRoomId(roomId).
				orElseThrow(()->new NotFoundException("Room not found: "+roomId));
		if (room.isGameActive()) {
		    throw new IllegalStateException("Game already active in this room!");
		}
		if(hostName.equals(room.getHost().getName())) {
		room.setTotalRounds(room.getPlayers().size());
		room.setCurrentRound(1);
		room.setGameActive(true);
		chatMessage.setAll("Round "+room.getCurrentRound()+" started!", "System", ChatType.SYSTEM);
		messagingTemplate.convertAndSend("/topic/room/"+roomId,chatMessage);
		String drawerName=roundService.prepareRound(room);
		roomRepository.save(room);	
		return ResponseEntity.ok(drawerName);
	}
		throw new IllegalStateException("Only host can start the game");
}
	
	// submission checking
	@Transactional
	public String submitGuess(String roomId, Long playerId, String guess) {
		ChatMessage chatMessage=new ChatMessage();
		long currentTime = System.currentTimeMillis();
		//checking for room
	    Room room = roomRepository.findByRoomId(roomId)
	            .orElseThrow(() -> new NotFoundException("Room not found: " + roomId));
	    //time elapsed
	    long elapsedSeconds = (currentTime - room.getRoundStartTime()) / 1000;
	    
	    //time left
		long timeLeft = room.getRoundDuration() - elapsedSeconds;
		
		
	    //checking for the player in the room
		Player player=playerRepository.findById(playerId)
					.orElseThrow(()->new NotFoundException("Player " + playerId + " is not in this room."));
		player.setLastActive(LocalDateTime.now());
		if (timeLeft <= 0) {
			chatMessage.setContent("Time's up! No more guesses allowed!");
			chatMessage.setTarget(player.getName()); //for private messaging
			messagingTemplate.convertAndSend("/topic/room/"+roomId,chatMessage);
	        return "Time's up! No more guesses allowed.";
	    }
		
	    //checking if drawer is submitting the answer
	    if (room.getCurrentDrawer() != null && room.getCurrentDrawer().getPlayerId().equals(playerId)) {
	    	chatMessage.setContent("Drawer cannot guess the word!");
	    	chatMessage.setName("System");
	    	chatMessage.setTarget(player.getName()); //for private messaging
			messagingTemplate.convertAndSend("/topic/room/"+roomId,chatMessage); 
	        return "Drawer cannot guess the word!";
	    }
	    
	    //checking if player already guessed.
	    if(room.getGuessedPlayers().contains(playerId)) {
	    	chatMessage.setContent("Already Guessed the word!");
	    	chatMessage.setTarget(player.getName()); //for private messaging
			messagingTemplate.convertAndSend("/topic/room/"+roomId,chatMessage);
	    	return player.getName()+": Already Guessed the word!";
	    }
	    
	    //checking the guess with the word
	    if (guess.equalsIgnoreCase(room.getCurrentWord().trim())) {
	    	int score = (int) ( timeLeft * 10);
			player.setScore(player.getScore() + score);
			room.getGuessedPlayers().add(playerId);
			playerRepository.save(player);
			chatMessage.setAll(player.getName()+" Guessed correctly!",player.getName(),ChatType.SYSTEM);
			messagingTemplate.convertAndSend("/topic/room/"+roomId,chatMessage);
			
			//if all players except the drawer have guessed we need to end the round and stop the scheduling
			if (room.getGuessedPlayers().size() == room.getPlayers().size() - 1) {
				ScheduledFuture<?> future = scheduledTasks.get(roomId);
				if(future != null) {
				    future.cancel(false); 
				    scheduledTasks.remove(roomId);
				}
				
				return gameFlowService.nextRoundOrEndGame(roomId); 
			}
	        return "Correct! Player " + playerId + " guessed the word.";
	    }
	    else {
	    	chatMessage.setAll(guess,player.getName(),ChatType.CHAT);
	    	messagingTemplate.convertAndSend("/topic/room/"+roomId,chatMessage);
	    	return "Incorrect guess!";
	    }
	}
}
