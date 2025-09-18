package com.rush.doodle.WebSocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.CrossOrigin;

import com.rush.doodle.exceptions.NotFoundException;
import com.rush.doodle.game.GameService;
import com.rush.doodle.player.Player;
import com.rush.doodle.player.PlayerRepository;
import com.rush.doodle.room.Room;
import com.rush.doodle.room.RoomRepository;

@Controller 
public class ChatController {
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired 
	private RoomRepository roomRepository;
	@Autowired 
	private PlayerRepository playerRepository;
	@Autowired
	private GameService gameService;
	@MessageMapping("/sendMessage/{roomId}")  //client sends message here
	public void sendMessage(@DestinationVariable String roomId,ChatMessage message) {
		if (message.getType() == ChatType.JOIN) {
            message.setContent(message.getName() + " has joined the room"); //player X has joined the room
        }
        else if (message.getType() == ChatType.LEAVE) {
            message.setContent(message.getName() + " has left the room");
        }
        else if(message.getType()==ChatType.CHAT){ // comes out from the client end
        	Player player=playerRepository.findByNameAndRoom_RoomId(message.getName(),roomId)
        					.orElseThrow(()->new NotFoundException("Player Not Found: "+message.getName()));
        	Room room=roomRepository.findByRoomId(roomId).get();
        	
        	if (room.getRoundStartTime() == null) {
                // ✅ Game not started → treat as normal chat
                message.setContent(message.getContent());
            } 
        	else {
        		gameService.submitGuess(roomId, player.getPlayerId(), message.getContent());
        		return;
        	}
        }
		 messagingTemplate.convertAndSend("/topic/room/"+roomId,message);//message is sent to all the clients subscribed to this.
	}
	@MessageMapping("/canvas/{roomId}")
	public void handleDraw(@DestinationVariable String roomId,@Payload DrawMessage drawMessage/*,Principal principal*/) {

	    messagingTemplate.convertAndSend("/topic/canvas/" + roomId, drawMessage);
	}
}
