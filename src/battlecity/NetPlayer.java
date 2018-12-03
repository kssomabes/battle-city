package battlecity;

import java.net.InetAddress;

public class NetPlayer {
	private InetAddress address;
	private int port;
	private double x = -1 , y = -1;
	private String name;
	
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
	
	public void setCoordinates(double x, double y) {
		this.x = x;
		this.y = y;
	} 
	
	public String getCoordinates() {
		return this.x + " " + this.y;
	}
}
