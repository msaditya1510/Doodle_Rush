package com.rush.doodle.game.round;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.rush.doodle.WebSocket.ChatMessage;
import com.rush.doodle.WebSocket.ChatType;

import com.rush.doodle.player.Player;
import com.rush.doodle.room.Room;
import com.rush.doodle.room.RoomRepository;

@Service
public class RoundService {
	
	 	private final RoomRepository roomRepository;
	    private final SimpMessagingTemplate messagingTemplate;
	    
	    
	public RoundService(RoomRepository roomRepository, SimpMessagingTemplate messagingTemplate) {
			super();
			this.roomRepository = roomRepository;
			this.messagingTemplate = messagingTemplate;
		}
	
	private static final List<String> WORDS = Arrays.asList("apple", "banana", "house", "car", "laptop", "guitar", "dog", "tree");
	
	//to prepare for the round
		public String prepareRound(Room room) {
			Player drawer=chooseDrawer(room);
			ChatMessage chatMessage=new ChatMessage();
			chatMessage.setAll("Drawer for this round is "+drawer.getName(),"System", ChatType.SYSTEM);
			messagingTemplate.convertAndSend("/topic/room/"+room.getRoomId(),chatMessage);
			room.setCurrentDrawer(drawer);
			roomRepository.save(room);	
			return room.getCurrentDrawer().getName();
		}
	
		public void roundCompletedMessage(Room room) {
			ChatMessage message=new ChatMessage();
			message.setAll("Round-"+room.getCurrentRound()+" has ended!", "System", ChatType.SYSTEM);
			messagingTemplate.convertAndSend("/topic/room/"+room.getRoomId(), message);
		}
		
		// to pick a random word
		public String pickRandomWord(Player player) {
			ChatMessage chatMessage=new ChatMessage();
			Random random=new Random();
			String word=WORDS.get(random.nextInt(WORDS.size()));
			chatMessage.setContent(word);
			chatMessage.setTarget(player.getName());
			messagingTemplate.convertAndSend("/topic/room/"+player.getRoom().getRoomId(),chatMessage);
			//This method is used to send this word only to the drawer.
			return word;
		}
		
		//method to choose the drawer
		public Player chooseDrawer(Room room) {
		    int index = room.getCurrentDrawerIndex();
		    int totalPlayers = room.getPlayers().size();

		    // Define "active cutoff" (e.g., last 1 minute)
		    LocalDateTime cutoff = LocalDateTime.now().minusMinutes(1);

		    for (int i = 0; i < totalPlayers; i++) {
		        Player candidate = room.getPlayers().get(index);

		        if (candidate.getLastActive() != null && candidate.getLastActive().isAfter(cutoff)) {
		            // Found an active drawer
		            room.setCurrentDrawer(candidate);
		            room.setCurrentDrawerIndex((index + 1) % totalPlayers);
		            return candidate;
		        }

		        // Move to next player if inactive
		        index = (index + 1) % totalPlayers;
		    }

		    // If no active players found
		    throw new IllegalStateException("No active players available to be drawer.");
		}



}
