package com.rush.doodle.game.core;

import com.rush.doodle.game.leaderboard.LeaderboardService;
import com.rush.doodle.game.round.RoundService;
import com.rush.doodle.player.PlayerDTO;
import com.rush.doodle.room.Room;
import com.rush.doodle.room.RoomRepository;
import com.rush.doodle.room.RoomService;
import com.rush.doodle.WebSocket.ChatMessage;
import com.rush.doodle.WebSocket.ChatType;
import com.rush.doodle.exceptions.NotFoundException;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameFlowService implements GameFlowHandler {
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final LeaderboardService leaderboardService;
    private final RoundService roundService;
    private final GameSchedulingService schedulingService;
    private final GameEventService gameEventService;
    // All dependencies injected via constructor: clear, immutable, testable.
    public GameFlowService(RoomRepository roomRepository,
                          SimpMessagingTemplate messagingTemplate,
                          LeaderboardService leaderboardService,
                          RoundService roundService,
                          GameSchedulingService schedulingService,RoomService roomService,
                          GameEventService gameEventService) {
        this.roomRepository = roomRepository;
        this.messagingTemplate = messagingTemplate;
        this.leaderboardService = leaderboardService;
        this.roundService = roundService;
        this.schedulingService = schedulingService;
        this.roomService=roomService;
        this.gameEventService=gameEventService;
    }
    
  //used for the start button
  		public String startRound(String roomId,String playerName) {
  			ChatMessage chatMessage=new ChatMessage();
  			Room room=roomRepository.findByRoomId(roomId).
  					orElseThrow(()->new NotFoundException("Room not found: "+roomId));
  			if(room.getCurrentDrawer().getName().equals(playerName)) {
  			chatMessage.setAll("Round-"+room.getCurrentRound()+" started! Drawer: "+room.getCurrentDrawer(), "System", ChatType.SYSTEM);
  			messagingTemplate.convertAndSend("/topic/room/"+roomId,chatMessage);
  			String currentRoundWord=roundService.pickRandomWord(room.getCurrentDrawer());
  			room.setCurrentWord(currentRoundWord);
  			room.setRoundStartTime(System.currentTimeMillis()); //saves the current time in seconds
  			roomRepository.save(room);	
  			// Schedule the end of the round to trigger the GameFlowService method
  			schedulingService.scheduleTask(
  			    roomId,
  			    () -> nextRoundOrEndGame(roomId), // This is the magic
  			    room.getRoundDuration()
  			);
  			return room.getCurrentWord();
  			}
  			throw new IllegalStateException("Only drawer can start the round");
  		}
    
    @Override
    @Transactional
    public String nextRoundOrEndGame(String roomId) {
        // Use our new dedicated scheduling service
        schedulingService.cancelTask(roomId);

        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NotFoundException("Room Not Found: " + roomId));

        roundService.roundCompletedMessage(room); // Delegate to RoundService

        room.setCurrentRound(room.getCurrentRound() + 1);
        room.resetGuessedPlayers();

        if (room.getCurrentRound() > room.getTotalRounds()) {
            // Game Over Logic
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setAll("Game Over!", "System", ChatType.SYSTEM);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMessage);
            gameEventService.broadcastLeaderboardUpdate(roomId);

            List<PlayerDTO> leaderboard = leaderboardService.getLeaderboard(roomId);
            // We'll fix the reset logic in the Room entity later
            roomService.resetRoom(room); // For now, it stays. We'll improve this next.

            return "Game Over! Final Leaderboard: " + leaderboard;
        }
        // Next Round Logic
        return roundService.prepareRound(room); // Delegate to RoundService
    }
}