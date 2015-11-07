/**
 * 
 */
package hw1opt2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.net.Socket;
import java.util.ArrayList;
import constants.*;

/**
 * @author Miguel
 *
 */
public class Actions implements Runnable {
	
	private Socket socket;
	private Peer peer;

	
	
	public Actions(Socket socket, Peer peer) {
		this.peer=peer;
		this.socket=socket;
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		StreamTokenizer rd=null;
		ArrayList<String> msg = new ArrayList<String>();
		try {
			rd = new StreamTokenizer ( new BufferedReader( new InputStreamReader( socket.getInputStream())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(rd==null){
			return;
		}
		int ret=StreamTokenizer.TT_EOF;
		try {
			ret = rd.nextToken();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//This loop reads the message. Everything in the message should be Strings
		while( ret != StreamTokenizer.TT_EOF && ret!=StreamTokenizer.TT_EOL){
			
			if(ret==StreamTokenizer.TT_WORD || ret==StreamTokenizer.TT_NUMBER){
				msg.add(rd.sval);//sval is a string no matter what
			}
			
			
			try {
				ret=rd.nextToken();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//Message read stored in ArralyList msg
		if(msg.get(0)==MessageTypes.SEND_MOVE){
			//Params are hostname, move
			peer.updateMove(msg.get(1),msg.get(2));
			//TODO call main thread
			
		}else if(msg.get(0)==MessageTypes.READY){
			peer.setPeerReady(msg.get(1),Boolean.valueOf(msg.get(2)));
			//Inside this call the check for all players ready will be made
			//TODO Review, will this be ready for a new match, if so call here main thread
			
		}else if(msg.get(0)==MessageTypes.BYE){
			peer.removePeer(msg.get(1));
			//TODO Do we need to call the main thread with a bye? 
			
		}else if(msg.get(0)==MessageTypes.HELLO){
			//Hostname, IP, port
			try {
				peer.putNewPeer(msg.get(1), msg.get(2),Integer.valueOf((msg.get(3))));
			} catch (NumberFormatException e) {
				System.out.println("Error converting number");
				e.printStackTrace();
				return;
			} catch (IOException e) {
				System.out.println("Error creating socket for hello message");
				e.printStackTrace();
				return;
			}
			
			
		}else if(msg.get(0)==MessageTypes.ACT_FAST){
			//TODO Here call main thread to pop up a warning
			
			
		}else if(msg.get(0)==MessageTypes.NEED_HOSTS){
			peer.sendHosts(msg.get(1));
			
		}else if(msg.get(0)==MessageTypes.HOST){
			try {
				peer.putNewPeer(msg.get(1), msg.get(2), Integer.valueOf(msg.get(3)));
			} catch (NumberFormatException e) {
				System.out.println("Error converting number");
				e.printStackTrace();
				return;
			} catch (IOException e) {
				System.out.println("Error creating socket for hello message");
				e.printStackTrace();
				return;
			}
			
		}else if(msg.get(0)==MessageTypes.ALIVE){
			//TODO Here we should stop the timeout process for that peer and create a new one, or restart the timer			
		}
		
		
	}

}
