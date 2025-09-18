package com.rush.doodle.player;

public class PlayerDTO {
	private long playerId;
	private String name;
	private int score;
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "PlayerDTO [playerId=" + playerId + ", name=" + name + ", score=" + score + "]";
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public PlayerDTO(long playerId, String name, int score) {
		super();
		this.playerId = playerId;
		this.name = name;
		this.score = score;
	}
}
