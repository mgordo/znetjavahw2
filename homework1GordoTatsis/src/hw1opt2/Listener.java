package hw1opt2;
import java.io.IOException;
import java.net.*;
public class Listener extends Thread {
	
	private Peer peer;
	InetAddress localIP;
	private int myport;
	private ServerSocket serversocket;
	
	public Listener(Peer peer, int port){
		this.peer=peer;
		try {
			localIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("Error getting local IP address in listener thread");
			e.printStackTrace();
		}
		myport=port;
		try {
			serversocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Error generating socket");
			e.printStackTrace();
		} 
		
		
	}
	
	@Override
	public void run(){
		
		while(true){
			Socket socket=null;
			try {
				socket = serversocket.accept();
			} catch (IOException e) {
				System.out.println("Failed to establish connection while listening");
				e.printStackTrace();
			}
			
			Actions newaction = new Actions(socket, peer);
			newaction.run();
			
		}
		
		
	}

}
