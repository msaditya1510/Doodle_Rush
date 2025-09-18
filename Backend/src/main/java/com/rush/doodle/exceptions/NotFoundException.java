package com.rush.doodle.exceptions;

@SuppressWarnings("serial")
public class NotFoundException extends RuntimeException{
	public NotFoundException(String message) {
		super(message);
	}
}
