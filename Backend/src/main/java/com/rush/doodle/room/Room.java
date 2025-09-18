package com.rush.doodle.room;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rush.doodle.player.Player;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
@Entity
public class Room {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	@Column(unique=true,nullable=false,length=6)
	private String roomId;
	@OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
	@JsonManagedReference
    private List<Player> players = new ArrayList<>();
	private String currentWord;
	private int currentRound;
	private int totalRounds;
	@ManyToOne
	@JoinColumn(name = "current_drawer_id")
	private Player currentDrawer;
	private int currentDrawerIndex=0;
	private final int roundDuration=60;
	// to store the start time
	private Long roundStartTime; 
	private boolean gameActive=false;
	private LocalDateTime lastActive;
	//Guessed set of players in the round
	@ElementCollection
	@CollectionTable(
		    name = "room_guessed_players",
		    joinColumns = @JoinColumn(name = "room_id")
		)
		@Column(name = "player_id")
	Set<Long> guessedPlayers=new HashSet<>();
	@ManyToOne
	private Player host;
	
	public Player getHost() {
		return host;
	}
	public void setHost(Player host) {
		this.host = host;
	}
	public Set<Long> getGuessedPlayers() {
		return guessedPlayers;
	}
	public void resetGuessedPlayers() {
		guessedPlayers.clear();
	}
	public Room(){
		setRoomId();
	}
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId() {
		roomId=roomIdGenerator();
	}
	public List<Player> getPlayers(){
		return players;
	}
	public void addPlayer(Player player) {
		if(players.size()<8) {
			players.add(player);
			player.setRoom(this);
		}
		else {
			throw new IllegalStateException("This Room Already has 8 players in it!");
		}
	}
	public void removePlayer(Player player) {
		players.remove(player);
		player.setRoom(null);
	}
	public String toString() {
		return "{\nRoomId: "+roomId+"\nPlayers count: "+players.size()+"\n}";
	}
	//For generating roomId
	public String roomIdGenerator() {
		int length=6;
		Random random=new Random();
		String characters="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<length;i++) {
			sb.append(characters.charAt(random.nextInt(characters.length())));
		}
		return sb.toString();
	}
	public Long getId() {
		return id;
	}
	public String getCurrentWord() {
		return currentWord;
	}
	public void setCurrentWord(String currentWord) {
		this.currentWord = currentWord;
	}
	public int getCurrentRound() {
		return currentRound;
	}
	public void setCurrentRound(int currentRound) {
		this.currentRound= currentRound;
	}
	public Player getCurrentDrawer() {
		return currentDrawer;
	}
	public void setCurrentDrawer(Player currentDrawer) {
		this.currentDrawer = currentDrawer;
	}
	public int getCurrentDrawerIndex() {
		return currentDrawerIndex;
	}
	public void setCurrentDrawerIndex(int currentDrawerIndex) {
		this.currentDrawerIndex = currentDrawerIndex;
	}
	public int getRoundDuration() {
		return roundDuration;
	}
	public Long getRoundStartTime() {
		return roundStartTime;
	}
	public void setRoundStartTime(Long roundStartTime) {
		this.roundStartTime = roundStartTime;
	}
	public int getTotalRounds() {
		return totalRounds;
	}
	public void setTotalRounds(int totalRounds) {
		this.totalRounds = totalRounds;
	}
	public boolean isGameActive() {
		return gameActive;
	}
	public void setGameActive(boolean gameActive) {
		this.gameActive = gameActive;
	}
	public LocalDateTime getLastActive() {
		return lastActive;
	}
	public void setLastActive(LocalDateTime lastActive) {
		this.lastActive = lastActive;
	}
	
}
