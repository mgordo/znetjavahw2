package hw1opt2;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
	
	private static final Executor sender = new ThreadPoolExecutor(1, 5, 1, TimeUnit.SECONDS,  new LinkedBlockingQueue<Runnable>());
	private static final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);
	
	public static void sendMessage(final Message m, final String to, final InetAddress ip, final int port){
		sender.execute(new Runnable() {
			public void run() {
				Socket socket = null;
				try {
					socket = new Socket(ip, port);
					PrintWriter wr = new PrintWriter(socket.getOutputStream());
					wr.write(m.toString());
					wr.flush();
					wr.close();
					socket.close();
				}
				catch (IOException e) {
					PRSGame.getInstance().removePeer(to);
				}
				finally{
					try{
						socket.close();
					}
					catch (Exception e) {
					}
				}
			}
		});
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
			
			sendMessage(new Message(), to, ip, port);//TODO: keep alive message
			futureHeartbeats.put(to, heartbeatScheduler.schedule(new HeartBeatRunnable(to, ip, port), heartbeatDelay, TimeUnit.MILLISECONDS));
		}
	}
}
