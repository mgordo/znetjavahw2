package hw1opt2;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MessageSender {
	private static Executor sender = new ThreadPoolExecutor(1, 5, 1, TimeUnit.SECONDS,  new LinkedBlockingQueue<Runnable>());
	
	public static void sendMessage(final Message m, final String to, final InetAddress ip, final int port){
		sender.execute(new Runnable() {
			public void run() {
				try {
					Socket socket = new Socket(ip, port);
					PrintWriter wr = new PrintWriter(socket.getOutputStream());
					wr.write(m.toString());
					wr.flush();
					wr.close();
				}
				catch (IOException e) {
					PRSGame.getInstance().removePeer(to);
				}
			}
		});
	}
}
