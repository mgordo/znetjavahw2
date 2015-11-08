package hw1opt2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Broadcast {
	private static final int port = 10807;
	private static final int delay = 5000;
	private static DatagramSocket listeningSocket;
	private static DatagramSocket broadcastingSocket;
	
	private static final BroadcastListener listener = new BroadcastListener();
	private static final Thread listenerThread = new Thread(listener);
	
	private static final ScheduledExecutorService broadcastScheduler = Executors.newScheduledThreadPool(1);
	private static ScheduledFuture<?> futureBroadcast = null;
	private static final Object broadcastingLock = new Object();
	
	//TODO: (optional) add timing information, discard games seen too far in the past
	private static final ConcurrentHashMap<String,GameInfo> gameList = new ConcurrentHashMap<String,GameInfo>();
	
	static{
		try{
			broadcastingSocket = new DatagramSocket();	
			listeningSocket = new DatagramSocket();		
			listeningSocket.setReuseAddress(true);
			listeningSocket.setBroadcast(true);
			listeningSocket.bind(new InetSocketAddress(port));
			listenerThread.setDaemon(true);
			listenerThread.start();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private static class BroadcastListener implements Runnable{

		@Override
		public void run() {
			while (true){
				try {
					byte[] buffer = new byte[100];
			        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			        listeningSocket.receive(packet);
					String message = new String(packet.getData()).trim();
					System.out.println("Received message");
					if (message.startsWith("P2PRPS")){
						String messageParts[] = message.split(" ");
						GameInfo info = new GameInfo(messageParts[1], packet.getAddress(), Integer.parseInt(messageParts[2]));
						gameList.put(info.name, info);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static Collection<GameInfo> getGames(){
		return gameList.values();
	}
	
	public static void setBroadcasting(boolean state){
		synchronized (broadcastingLock) {
			
			if (!state && (futureBroadcast != null)){
				futureBroadcast.cancel(false);
				futureBroadcast = null;
			}
			if (state && (futureBroadcast == null || futureBroadcast.isDone())){
				futureBroadcast = broadcastScheduler.schedule(broadcastTask, delay, TimeUnit.SECONDS);
			}
		}
	}
	
	private final static Runnable broadcastTask = new Runnable() {
		@Override
		public void run() {
			

			byte[] broadcastMessage = ("P2PRPS "+PRSGame.getInstance().getName()+" "+PRSGame.getInstance().getPort()).getBytes();
			
			try {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface netInterface = interfaces.nextElement();
					
					for (InterfaceAddress address : netInterface.getInterfaceAddresses()) {
						if (address.getBroadcast() == null)
							continue;
						
						try {
							//TODO: replace IP below with actual IP, taken from TCP listener class 
							DatagramPacket broadcastPacket = new DatagramPacket(broadcastMessage, broadcastMessage.length, address.getBroadcast(), port);
							broadcastingSocket.send(broadcastPacket);
							System.out.println("Sent message");
						}
						catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			}
			catch (SocketException e) {
				e.printStackTrace();
			}

			synchronized (broadcastingLock) {
				futureBroadcast = broadcastScheduler.schedule(broadcastTask, delay, TimeUnit.SECONDS);
			}
		}
	};
	
	public static class GameInfo{
		public final String name;
		public final InetAddress address;
		public final int port;
		
		public GameInfo(String name, InetAddress address, int port){
			this.name = name;
			this.address = address;
			this.port = port;
		}
		
	}
	
}
