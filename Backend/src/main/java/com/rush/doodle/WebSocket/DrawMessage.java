package com.rush.doodle.WebSocket;

public class DrawMessage {
	private double startX;
	private double startY;
	private double endX;
	private double endY;
	private String color;
	private double thickness;
	public double getStartX() {
		return startX;
	}
	public void setStartX(double startX) {
		this.startX = startX;
	}
	public double getStartY() {
		return startY;
	}
	public void setStartY(double startY) {
		this.startY = startY;
	}
	public double getEndX() {
		return endX;
	}
	
	public DrawMessage() {
	}
	public DrawMessage(double startX,double startY, double endX, double endY, String color, double thickness) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.color = color;
		this.thickness = thickness;
	}
	public void setEndX(double endX) {
		this.endX = endX;
	}
	public double getEndY() {
		return endY;
	}
	public void setEndY(double endY) {
		this.endY = endY;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public double getThickness() {
		return thickness;
	}
	@Override
	public String toString() {
		return "DrawMessage [startX=" + startX + ", startY=" + startY + ", endX=" + endX + ", endY=" + endY + ", color="
				+ color + ", thickness=" + thickness + "]";
	}
	public void setThickness(double thickness) {
		this.thickness = thickness;
	}
	
}
