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
import java.util.Arrays;

import constants.*;

/**
 * @author Miguel
 *
 */
public class MessageParser extends Thread {
	
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
		
		String str=null;
		try {
			BufferedReader buffrd = new BufferedReader( new InputStreamReader(socket.getInputStream()));
			str = buffrd.readLine();
			
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		if(str==null){
			try {
				socket.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			return;
		}
		String[] msg2 = str.split(" ");
		ArrayList<String> msg = new ArrayList<String>(Arrays.asList(msg2));

		//Message read stored in ArralyList msg
		if(msg.get(0).equals(MessageTypes.SEND_MOVE)){
			//SEND_MOVE, hostname, Move
			
			gameListening.moveMade(msg.get(1), msg.get(2));

		}else if(msg.get(0).equals(MessageTypes.READY)){
			//READY hostname
			
			gameListening.peerIsReady(msg.get(1));
					
		}else if(msg.get(0).equals(MessageTypes.BYE)){
			//BYE hostname
			
			gameListening.removePeer(msg.get(1));
			
		}else if(msg.get(0).equals(MessageTypes.HELLO)){
			//HELLO Hostname, IP, port
			
			gameListening.putNewPeer(msg.get(1), msg.get(2),Integer.valueOf((msg.get(3))));

		}else if(msg.get(0).equals(MessageTypes.ACT_FAST)){
			//ACT_FAST
			
			gameListening.actFast();
			
		}else if(msg.get(0).equals(MessageTypes.NEED_HOSTS)){
			//NEED_HOSTS hostname
			//No need to call main thread for sending hosts
			peer.sendHosts(msg.get(1));

		}else if(msg.get(0).equals(MessageTypes.HOST)){
			//HOST hostname ip port
			
			gameListening.addHost(msg.get(1), msg.get(2), Integer.valueOf(msg.get(3)));
			
		}else if(msg.get(0).equals(MessageTypes.ALIVE)){
			//ALIVE hostname
			
			gameListening.hostAlive(msg.get(1));
			//TODO Here we should stop the timeout process for that peer and create a new one, or restart the timer			
		}
		try {
			socket.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}
