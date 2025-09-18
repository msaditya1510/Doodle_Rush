package com.rush.doodle.game.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rush.doodle.WebSocket.ChatMessage;
import com.rush.doodle.WebSocket.ChatType;
import com.rush.doodle.game.leaderboard.LeaderboardService;
import com.rush.doodle.player.PlayerDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameEventService {


    private final SimpMessagingTemplate messagingTemplate;
    private final LeaderboardService leaderboardService;
    private final ObjectMapper objectMapper; // For converting objects to JSON

    public GameEventService(SimpMessagingTemplate messagingTemplate,
                           LeaderboardService leaderboardService,
                           ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.leaderboardService = leaderboardService;
        this.objectMapper = objectMapper;
    }

    public void broadcastLeaderboardUpdate(String roomId) {
        // 1. Get the latest leaderboard data
        List<PlayerDTO> leaderboard = leaderboardService.getLeaderboard(roomId);

        // 2. Create a message
        ChatMessage updateMessage = new ChatMessage();
        updateMessage.setType(ChatType.SCORE_UPDATE);
        updateMessage.setName("System");

        try {
            // 3. Put the leaderboard data into the message content as JSON
            String leaderboardJson = objectMapper.writeValueAsString(leaderboard);
            updateMessage.setContent(leaderboardJson);
        } catch (JsonProcessingException e) {
            updateMessage.setContent("[]"); // Send empty array on error
            // You should also log this error: log.error("Failed to serialize leaderboard", e);
        }

        // 4. Broadcast it to everyone in the room
        messagingTemplate.convertAndSend("/topic/room/" + roomId, updateMessage);
    }
}