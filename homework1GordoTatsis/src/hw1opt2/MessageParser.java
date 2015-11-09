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
public class MessageParser implements Runnable {
	
	private Socket socket;
	private Peer peer;
	private PRSGame gameListening;
	
	
	public MessageParser(Socket socket, Peer peer,PRSGame game) {
		this.peer=peer;
		this.socket=socket;
		this.gameListening = game;
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
			
			gameListening.moveMade(msg.get(1), msg.get(2));
			
		}else if(msg.get(0)==MessageTypes.READY){
			gameListening.peerIsReady(msg.get(1));
					
		}else if(msg.get(0)==MessageTypes.BYE){
			gameListening.removePeer(msg.get(1));
			
			
		}else if(msg.get(0)==MessageTypes.HELLO){
			//Hostname, IP, port
			
			gameListening.putNewPeer(msg.get(1), msg.get(2),Integer.valueOf((msg.get(3))));
			
			
			
			
		}else if(msg.get(0)==MessageTypes.ACT_FAST){
			
			gameListening.actFast();
			
		}else if(msg.get(0)==MessageTypes.NEED_HOSTS){
			//No need to call main thread for sending hosts
			peer.sendHosts(msg.get(1));
			
		}else if(msg.get(0)==MessageTypes.HOST){
			
			gameListening.addHost(msg.get(1), msg.get(2), Integer.valueOf(msg.get(3)));
			
		}else if(msg.get(0)==MessageTypes.ALIVE){
			
			gameListening.hostAlive(msg.get(1));
			//TODO Here we should stop the timeout process for that peer and create a new one, or restart the timer			
		}
		
		
	}

}
