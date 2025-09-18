package com.rush.doodle.player;

public class PlayerJoinRequestDTO {
	private String playerName;
	private String roomId;
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String name) {
		this.playerName = name;
	}
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
}
