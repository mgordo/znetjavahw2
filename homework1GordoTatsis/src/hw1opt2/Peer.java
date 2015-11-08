/**
 * 
 */
package hw1opt2;
import constants.*;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;

;

/** @brief Main class for a Peer
 * @author Miguel
 *
 */
public class Peer {
	
	private States state=States.NOT_CONNECTED;
	private ConcurrentHashMap<String,Socket> socket_list;
	private ConcurrentHashMap<String,String> movement_list;
	private ConcurrentHashMap<String,Boolean> ready_list;
	private ArrayList<String> peer_names;
	private int score;
	private Listener listener_thread;
	private String myhostname;
	
	/**
	 * Constructor for Peer class. Starts Listener Thread
	 * @param hostname Name of the Peer being instantiated
	 * @param port Port in which the peer will listen to other peers
	 */
	public Peer(String hostname,int port){
		this.myhostname=hostname;
		socket_list = new ConcurrentHashMap<String,Socket>();
		movement_list = new ConcurrentHashMap<String,String>();
		ready_list = new ConcurrentHashMap<String,Boolean>();
		peer_names = new ArrayList<String>();
		listener_thread = new Listener(this,port);
		listener_thread.run();
	}
	
		
	/**
	 * Gets the HashMap that connects the hostname of another peer with their listening address
	 * @return Concurrent HashMap with peer's hostnames as keys and Sockets as value
	 */
	public ConcurrentHashMap<String,Socket> getSocket_list() {
		return socket_list;
	}

	
	/**
	 * This method adds a new peer to the p2p network
	 * @param hostname The name of the host joining
	 * @param ip A String with the IP Address of the host joining
	 * @param port The port at which the host joining is listening
	 * @throws IOException
	 */
	public void putNewPeer(String hostname, String ip, int port) throws IOException {
		InetAddress ip_dest = InetAddress.getByName(ip);
		Socket socket = new Socket(ip_dest,port);
		this.socket_list.put(hostname, socket);
		this.peer_names.add(hostname);
		this.movement_list.put(hostname, Moves.NONE);
		ready_list.put(hostname, false);//TODO a new peer is by default not ready
	}

	
	/**
	 * This method updates the move of one of the connected peers
	 * @param move One of the possible moves specified in constants.Moves
	 * @param hostname Name of the hostname making the move
	 */
	public void setPeerMovement(String move, String hostname){
		this.movement_list.replace(hostname,move);
	}
	
	
	/**
	 * This method deletes a Peer from the P2P network. Each peer will call this method when they recieve a BYE message
	 * @param hostname Name of the host disconnecting
	 */
	public void removePeer(String hostname){
		this.movement_list.remove(hostname);
		this.socket_list.remove(hostname);
		this.peer_names.remove(peer_names.indexOf(hostname));
		ready_list.remove(hostname);
	}

	
	/**
	 * Returns own hostname
	 * @return This Peer's hostname
	 */
	public String getMyhostname() {
		return myhostname;
	}

	/**
	 * Sets this Peer's hostname
	 * @param myhostname New hostname for this Peers
	 */
	public void setMyhostname(String myhostname) {
		this.myhostname = myhostname;
	}
	
	/**
	 * Returns the current state of this peer, which can be any of thos defined in constants.States
	 * @return Any of the states defined in constants.States
	 */
	public synchronized States getState() {
		return state;
	}

	
	/**
	 * Changes the state of this peer to any of the states defined in constants.States
	 * @param state
	 */
	public synchronized void setState(States state) {
		this.state = state;
	}


	/**
	 * This method registers a move from another peer
	 * @param hostname The name of the peer making the move
	 * @param move The move of the peer who sent the notification, which can be any of those defined in constants.Moves
	 */
	public void updateMove(String hostname, String move) {
		// TODO Auto-generated method stub
		movement_list.replace(hostname, move);
		
	}

	
	/**
	 * Returns the ConcurrentHashMap that maps hostnames to a boolean indicating wether or not they are ready
	 * @return ConcurrentHashMap mapping hostnames to their readiness (True or False)
	 */
	public ConcurrentHashMap<String,Boolean> getReady_list() {
		return ready_list;
	}

	/**
	 * This method updates the readiness of a peer
	 * @param hostname Peer to update
	 * @param is_ready False if it's not ready, True otherwise
	 */
	public void setPeerReady(String hostname, boolean is_ready) {
		ready_list.replace(hostname, is_ready);
	}


	/**
	 * Send all hosts with their sockets to hostname
	 * @param hostname Hostname to whom to send the peer list;
	 */
	public void sendHosts(String hostname) {
		Socket socket = this.socket_list.get(hostname);
		try {
			PrintWriter wr = new PrintWriter(socket.getOutputStream());
			for(String peer: peer_names){
				Socket socket_to_be_sent = socket_list.get(peer);
				wr.write(MessageTypes.HOST+" "+peer+" "+socket_to_be_sent.getInetAddress().toString()+" "+Integer.toString(socket_to_be_sent.getPort()));
				wr.flush();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

}
