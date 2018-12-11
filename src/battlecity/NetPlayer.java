package battlecity;

import java.net.InetAddress;

public class NetPlayer {
	private InetAddress address;
	private int port;
	private double x = -1 , y = -1;
	private String name;
	private int lastDirection;
	private String color;
	
	public NetPlayer(String name, InetAddress address, int port){
		this.address = address;
		this.port = port;
		this.name = name;
	}

	public InetAddress getAddress(){
		return address;
	}

	public int getPort(){
		return port;
	}

	public String getName(){
		return name;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public String getColor(){
		return color;
	}
	
	public void setCoordinates(double x, double y) {
		this.x = x;
		this.y = y;
	} 
	
	public String getCoordinates() {
		return this.x + " " + this.y;
	}
	
	public void setLastDirection(int lastDirection) {
		this.lastDirection = lastDirection;
	}
	
	public int getLastDirection() {
		return lastDirection;
	}
	
	public String toString() {
		String retval = "";
		retval += "PLAYER ";
		retval += name + " ";
		retval += x + " ";
		retval += y +" ";
		retval += lastDirection + " ";
		retval += color;;
		return retval;
	}
}
