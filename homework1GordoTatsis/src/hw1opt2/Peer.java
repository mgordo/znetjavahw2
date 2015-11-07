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

/** @brief Main class for peers 
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
	 * Constructor
	 * @param hostname
	 */
	public Peer(String hostname,int port){
		this.myhostname=hostname;
		socket_list = new ConcurrentHashMap<String,Socket>();
		movement_list = new ConcurrentHashMap<String,String>();
		ready_list = new ConcurrentHashMap<String,Boolean>();
		peer_names = new ArrayList<String>();
	}
	
	
	

	public ConcurrentHashMap<String,Socket> getSocket_list() {
		return socket_list;
	}

	public void putNewPeer(String hostname, String ip, int port) throws IOException {
		InetAddress ip_dest = stringToIpAddress(ip);
		Socket socket = new Socket(ip_dest,port);
		this.socket_list.put(hostname, socket);
		this.peer_names.add(hostname);
		this.movement_list.put(hostname, Moves.NONE);
		ready_list.put(hostname, false);//TODO a new peer is by default not ready
	}
	
	private InetAddress stringToIpAddress(String ip) {
		// TODO Convert a string to a InetAddress
		return null;
	}




	public void setPeerMovement(String move, String hostname){
		this.movement_list.replace(hostname,move);
	}
	
	public void removePeer(String hostname){
		this.movement_list.remove(hostname);
		this.socket_list.remove(hostname);
		this.peer_names.remove(peer_names.indexOf(hostname));
		ready_list.remove(hostname);
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



	/**
	 * This method registers a move from another peer
	 * @param hostname
	 * @param move
	 */
	public void updateMove(String hostname, String move) {
		// TODO Auto-generated method stub
		movement_list.replace(hostname, move);
		
	}





	public ConcurrentHashMap<String,Boolean> getReady_list() {
		return ready_list;
	}




	public void setPeerReady(String hostname, boolean is_ready) {
		ready_list.replace(hostname, is_ready);
	}



	/**
	 * Send all hosts with their sockets to hostname
	 * @param hostname
	 */
	public void sendHosts(String hostname) {
		Socket socket = this.socket_list.get(hostname);
		try {
			PrintWriter wr = new PrintWriter(socket.getOutputStream());
			wr.write(MessageTypes.HOST+" "+hostname+" "+socket.getInetAddress().toString()+" "+Integer.toString(socket.getPort()));
			wr.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

}
