package battlecity;

import java.net.InetAddress;

public class NetPlayer {
	private InetAddress address;
	private int port;
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
}
