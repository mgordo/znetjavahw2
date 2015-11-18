package hw1opt2;
import java.io.IOException;
import java.net.*;
public class Listener extends Thread {
	

	InetAddress localIP;
	private int myport;
	private ServerSocket serversocket;
	private PRSGame gameListening;
	/*public Listener(Peer peer, int port){
		this.peer=peer;
		gameListening=null;
		try {
			localIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("Error getting local IP address in listener thread");
			e.printStackTrace();
		}
		myport=port;
		try {
			serversocket = new ServerSocket(myport);
		} catch (IOException e) {
			System.out.println("Error generating listening socket");
			e.printStackTrace();
		} 
		
		
	}*/
	
	
	public Listener(int port, PRSGame game){
		
		gameListening = game;
		try {
			localIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("Error getting local IP address in listener thread");
			e.printStackTrace();
		}
		myport=port;
		game.setIpAddress(localIP);
		try {
			serversocket = new ServerSocket(myport);
		} catch (IOException e) {
			System.out.println("Error generating listening socket");
			PRSGame.getInstance().TCPListenerError(e.getMessage());
		} 
		
		
	}
	
	@Override
	public void run(){
		//TODO We will probably need to change this for when we need to close the thread
		while(true){
			Socket socket=null;
			try {
				socket = serversocket.accept();
				MessageParser newaction = new MessageParser(socket, gameListening);
				newaction.setPriority(newaction.getPriority()+1);
				newaction.start();
			} catch (IOException e) {
				System.out.println("Failed to establish connection while listening");
				e.printStackTrace();
			}
			
			
			
		}
		
		
	}

}
