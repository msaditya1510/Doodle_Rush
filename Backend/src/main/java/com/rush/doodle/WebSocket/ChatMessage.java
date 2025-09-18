package com.rush.doodle.WebSocket;

import org.springframework.stereotype.Component;

//import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Component
public class ChatMessage {
	public void setAll(String content, String name, ChatType type) {
//		super();
		this.content = content;
		this.name = name;
		this.type = type;
		target=null;
	}
	private String content;
	private String name;
	private ChatType type;
	private String target;
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ChatType getType() {
		return type;
	}
	public void setType(ChatType type) {
		this.type = type;
	}
}
