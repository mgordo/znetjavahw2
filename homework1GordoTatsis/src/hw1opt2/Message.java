package hw1opt2;


import java.util.Map;

import constants.*;
public class Message {
	
	private String msgtype;
	private String from;
	private Object data;
	
	/**
	 * Create a new, empty message
	 * @param myhostname String with own hostname
	 */
	private Message(String from, String type){
		this.setFrom(from);
		this.setMsgtype(type);
		
	}
	
	/**
	 * Modifies the MessageObject being called to a Ready Message
	 */
	public static Message makeReadyMessage(String from){
		Message msg = new Message(from,MessageTypes.READY);
		msg.setData(null);
		return msg;
	}
	
	/**
	 * Modifies the MessageObject being called to a Bye Message
	 */
	public static Message makeByeMessage(String from){
		Message msg = new Message(from,MessageTypes.BYE);
		msg.setData(null);
		return msg;
		
	}
	
	/**
	 * Modifies the MessageObject being called to a NeedHosts Message
	 */
	public static Message makeNeedInfoMessage(String from){
		Message msg = new Message(from,MessageTypes.NEED_INFO);
		msg.setData(null);
		return msg;
		
	}
	
	/**
	 * Modifies the MessageObject being called to an Alive Message
	 */
	public static Message makeAliveMessage(String from){
		Message msg = new Message(from,MessageTypes.ALIVE);
		msg.setData(null);
		return msg;
		
	}
	
	/**
	 * Modifies the MessageObject being called to an SendMove Message
	 * @param myMove Move to be sent
	 */
	public static Message makeSendMoveMessage(String from, String myMove){
		Message msg = new Message(from,MessageTypes.SEND_MOVE);
		msg.setData(new String(myMove));
		return msg;
	}
	
	/**
	 * Modifies the MessageObject being called to a Hello Message
	 * @param ip Own ip address in string format
	 * @param port port in which the p2p application will listen
	 */
	public static Message makeHelloMessage(String from, int port){
		Message msg = new Message(from,MessageTypes.HELLO);
		msg.setData(new Integer(port));
		return msg;
	}
	
	/**
	 * Modifies the MessageObject being called to an ActFast Message
	 */
	public static Message makeActFastMessage(String from){
		Message msg = new Message(from,MessageTypes.ACT_FAST);
		msg.setData(null);
		return msg;
	}
	


	/**
	 * 
	 * @param state State of the game
	 * @param score A string with hostname + score for every hostname in the game, all in one string
	 */
	public static Message makeInfo(String from, Map<String,PeerInformation> info){
		Message msg = new Message(from,MessageTypes.INFO);
		msg.setData(info);
		return msg;
	}

	public String getMsgtype() {
		return msgtype;
	}

	private void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}

	public String getFrom() {
		return from;
	}

	private void setFrom(String from) {
		this.from = from;
	}

	public Object getData() {
		return data;
	}

	private void setData(Object data) {
		this.data = data;
	}
	
	

}
