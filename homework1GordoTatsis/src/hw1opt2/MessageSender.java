package hw1opt2;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MessageSender {
	private static final int heartbeatDelay = 5000;
	private static final Map<String,ScheduledFuture<?>> futureHeartbeats = new ConcurrentHashMap<String,ScheduledFuture<?>>();
	
	private static final Map<String,Queue<Runnable>> messageQueues = new HashMap<String,Queue<Runnable>>();
	private static final Executor sender = new ThreadPoolExecutor(1, 10, 1, TimeUnit.SECONDS,  new LinkedBlockingQueue<Runnable>());
	private static final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);
	
	public static void sendMessageToAllPeers(final Message m){
		Set<String> recipients = PRSGame.getInstance().getPeerSet();
		String playerName = PRSGame.getInstance().getName();
		
		for (String peerName:recipients){
			if (peerName.equals(playerName))
				continue;
			sendMessage(m, peerName);
		}
	}
	
	public static void sendMessage(final Message m, final String to, final String ipString, final String portString){
		try {
			InetAddress ip = InetAddress.getByName(ipString);
			int port = Integer.valueOf(portString);
			sendMessage( m, to, ip, port);
		}
		catch (Exception e) {
			//TODO: if initial hello message, notify PRSGame
			PRSGame.getInstance().removePeer(to);
		}
	}
	
	public static void sendMessage(final Message m, final String to){
		PeerInformation peerInfo = PRSGame.getInstance().getPeerInformation(to);
		sendMessage(m, to, peerInfo.getIpAddress(), peerInfo.getPort());
	}
	
	public static void sendMessage(final Message m, final String to, final InetAddress address, final int port){

		if (messageQueues.get(to) == null)
			messageQueues.put(to, new ConcurrentLinkedQueue<Runnable>());
		final Queue<Runnable> messageQueue = messageQueues.get(to);
		
		Runnable messageRunnable = new Runnable() {
			public void run() { 
				if (messageQueue.isEmpty())
					messageQueue.add(this);
				else;
					
				Socket socket = null;
				try {
					socket = new Socket(address, port);
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					out.writeObject(m);
					out.flush();
					out.close();
					socket.close();
					System.out.println("DBG: MessageSender:  Sent "+m.getMsgtype()+" to "+to);
				}
				catch (IOException e) {
					//TODO: if initial hello message, notify PRSGame
					PRSGame.getInstance().removePeer(to);
					System.err.println("DBG: MessageSender:  Failed to send "+m.getMsgtype()+" to "+to);
				}
				finally{
					try{
						socket.close();
					}
					catch (Exception e) {
					}
				}
				messageQueue.remove();
				
				if (!messageQueue.isEmpty())
					sender.execute(messageQueue.peek());
			}
		};
		
		messageQueue.add(messageRunnable);
		

		if (messageQueue.size() == 1)
			sender.execute(messageRunnable);
	}
	
	public static void startHeartbeat(final String to, final InetAddress ip, final int port){
		ScheduledFuture<?> futureBeat = futureHeartbeats.get(to);
		if (futureBeat != null && !futureBeat.isDone())
			return;
		futureBeat = heartbeatScheduler.schedule(new HeartBeatRunnable(to, ip, port), heartbeatDelay, TimeUnit.MILLISECONDS);
		futureHeartbeats.put(to, futureBeat);
	}
	
	
	public static void stopHeartbeat(final String to){
		ScheduledFuture<?> futureBeat = futureHeartbeats.get(to);
		if (futureBeat == null || futureBeat.isDone())
			return;
		futureBeat.cancel(false);
	}
	
	private static class HeartBeatRunnable implements Runnable {
		private final String to;
		private final InetAddress ip;
		private final int port;

		public HeartBeatRunnable (final String to, final InetAddress ip, final int port){
			this.to = to;
			this.ip = ip;
			this.port = port;
		}
		public void run() {
			Message m = Message.makeAliveMessage(this.to);
			sendMessage(m, to, ip, port);
			futureHeartbeats.put(to, heartbeatScheduler.schedule(new HeartBeatRunnable(to, ip, port), heartbeatDelay, TimeUnit.MILLISECONDS));
		}
	}
}
