/**
 * 
 */
package hw1opt2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

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
			//HELLO Hostname
			
			gameListening.putNewPeer(msg.get(1), socket.getInetAddress().toString(),socket.getPort());

		}else if(msg.get(0).equals(MessageTypes.ACT_FAST)){
			//ACT_FAST
			
			gameListening.actFast();
			
		}else if(msg.get(0).equals(MessageTypes.NEED_HOSTS)){
			//NEED_HOSTS hostname
			//No need to call main thread for sending hosts
			String destination = peer.getAddress_list().get(msg.get(1));
			String[] destination_parts = destination.split(" ");
			for(String itpeer: peer.getAddress_list().keySet()){
				Message hostmsg= new Message(peer.getMyhostname());
				String address = peer.getAddress_list().get(itpeer);
				String[] address_parts = address.split(" ");
				hostmsg.makeHostMessage(itpeer, address_parts[0], Integer.parseInt(address_parts[1]));
				
				
				try {
					MessageSender.sendMessage(hostmsg, msg.get(1), InetAddress.getByName(destination_parts[0]), Integer.parseInt(destination_parts[1]));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing port while sending host");
					e.printStackTrace();
				} catch (UnknownHostException e) {
					System.out.println("Error while sending host");
					e.printStackTrace();
				}		
			}
			

		}else if(msg.get(0).equals(MessageTypes.HOST)){
			//HOST hostname ip port
			
			gameListening.addHost(msg.get(1), msg.get(2), Integer.valueOf(msg.get(3)));
			
		}else if(msg.get(0).equals(MessageTypes.ALIVE)){
			//ALIVE hostname
			
			gameListening.hostAlive(msg.get(1));
						
		}else if(msg.get(0).equals(MessageTypes.NEED_INFO)){
			//NEED_INFO hostname
			String score=peer.getMyhostname()+" "+Integer.toString(peer.getScore());
			for(String otherpeer: peer.getScore_list().keySet()){
				score = score+" "+otherpeer+" "+Integer.toString(peer.getScore_list().get(otherpeer));
			}
			Message infomsg= new Message(peer.getMyhostname());
			infomsg.makeInfo(peer.getState(),score);
			ConcurrentHashMap<String, String> address_list = peer.getAddress_list();
			String[] address = address_list.get(msg.get(1)).split(" ");
			try {
				MessageSender.sendMessage(infomsg, msg.get(1), InetAddress.getByName(address[0]), Integer.parseInt(address[1]));
			} catch (NumberFormatException e) {
				System.out.println("Error sending info and converting number");
				e.printStackTrace();
			} catch (UnknownHostException e) {
				System.out.println("Error sending info");
				e.printStackTrace();
			}
		}else if(msg.get(0).equals(MessageTypes.INFO)){
				int state = Integer.parseInt(msg.get(1));
				HashMap<String,Integer> scores = new HashMap<String, Integer>();
				String host;
				for(int i=3;i<msg.size();i+=2){
					scores.put(msg.get(i-1), Integer.parseInt(msg.get(i)));
				}
				gameListening.arrivedInfo(constants.State.values()[state], scores);
		
			
					
		}
		try {
			socket.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}
