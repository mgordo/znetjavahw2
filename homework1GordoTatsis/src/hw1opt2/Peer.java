/**
 * 
 */
package hw1opt2;
import constants.*;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.net.*;



/** @brief Main class for peers 
 * @author Miguel
 *
 */
public class Peer {
	
	private States state=States.NOT_CONNECTED;
	private ConcurrentHashMap<String,Socket> socket_list;
	private ConcurrentHashMap<String,String> movement_list;
	private ArrayList<String> peer_names;
	private int score;
	private Listener listener_thread;
	private String myhostname;
	
	/**
	 * Constructor
	 * @param hostname
	 */
	public Peer(String hostname){
		
	}
	
	
	

	public ConcurrentHashMap<String,Socket> getSocket_list() {
		return socket_list;
	}

	public void putNewPeer(String hostname, Socket socket) {
		this.socket_list.put(hostname, socket);
		this.peer_names.add(hostname);
		this.movement_list.put(hostname, Moves.NONE);
	}
	
	public void setPeerMovement(String move, String hostname){
		this.movement_list.replace(hostname,move);
	}
	
	public void removePeer(String hostname){
		this.movement_list.remove(hostname);
		this.socket_list.remove(hostname);
		this.peer_names.remove(peer_names.indexOf(hostname));
	}

	public String getMyhostname() {
		return myhostname;
	}

	public void setMyhostname(String myhostname) {
		this.myhostname = myhostname;
	}
	public synchronized States getState() {
		return state;
	}

	public synchronized void setState(States state) {
		this.state = state;
	}

}
