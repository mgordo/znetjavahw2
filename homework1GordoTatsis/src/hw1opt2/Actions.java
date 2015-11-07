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
			
			if(ret==StreamTokenizer.TT_WORD){
				msg.add(rd.sval);
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
			peer.updateMove(msg.get(1),msg.get(2));
			//TODO call main thread
			
		}else if(msg.get(0)==MessageTypes.READY){
			
		}else if(msg.get(0)==MessageTypes.BYE){
			
		}else if(msg.get(0)==MessageTypes.HELLO){
			
		}else if(msg.get(0)==MessageTypes.ACT_FAST){
			
			
		}else if(msg.get(0)==MessageTypes.ALIVE){
			//TODO Here we should stop the timeout process for that peer and create a new one, or restart the timer			
		}
		
		
	}

}
