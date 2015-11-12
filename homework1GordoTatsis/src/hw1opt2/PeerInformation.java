package hw1opt2;

import java.io.Serializable;
import java.net.InetAddress;

import constants.Move;

public class PeerInformation implements Serializable{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4659173952192446700L;
	private int score;
	private InetAddress ip_address;
	private int port;
	private boolean ready;
	private Move myMove;
	
	public PeerInformation(int score, InetAddress ip, int port, boolean ready, String move){
		this.setScore(score);
		this.setIpAddress(ip);
		this.setPort(port);
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public InetAddress getIpAddress() {
		return ip_address;
	}

	public void setIpAddress(InetAddress ip_address) {
		this.ip_address = ip_address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
