package hw1opt2;


import constants.*;
public class Message {
	
	private String message;
	private String myhostname;
	
	/**
	 * Create a new, empty message
	 * @param myhostname String with own hostname
	 */
	public Message(String myhostname){
		this.myhostname=myhostname;
		message=null;
		
	}
	
	/**
	 * Modifies the MessageObject being called to a Ready Message
	 */
	public void makeReadyMessage(){
		message = MessageTypes.READY+" "+myhostname;
	}
	
	/**
	 * Modifies the MessageObject being called to a Bye Message
	 */
	public void makeByeMessage(){
		message = MessageTypes.BYE+" "+myhostname;
		
	}
	
	/**
	 * Modifies the MessageObject being called to a NeedHosts Message
	 */
	public void makeNeedHostsMessage(){
		message = MessageTypes.NEED_HOSTS+" "+myhostname;
		
	}
	
	/**
	 * Modifies the MessageObject being called to an Alive Message
	 */
	public void makeAliveMessage(){
		message = MessageTypes.ALIVE+" "+myhostname;
		
	}
	
	/**
	 * Modifies the MessageObject being called to an SendMove Message
	 * @param myMove Move to be sent
	 */
	public void makeSendMoveMessage(String myMove){
		message = MessageTypes.SEND_MOVE+" "+myhostname+" "+myMove;
	}
	
	/**
	 * Modifies the MessageObject being called to a Hello Message
	 * @param ip Own ip address in string format
	 * @param port port in which the p2p application will listen
	 */
	public void makeHelloMessage(String ip, int port){
		message = MessageTypes.HELLO+" "+myhostname+" "+ip+" "+Integer.toString(port);
	}
	
	/**
	 * Modifies the MessageObject being called to an ActFast Message
	 */
	public void makeActFastMessage(){
		message = MessageTypes.ACT_FAST;
	}
	
	/**
	 * Modifies the MessageObject being called to a Host Message
	 * @param otherHostname Name of the host whose info is being sent in this message
	 * @param ip ip of otherHostname
	 * @param port port where otherHostname is listening
	 */
	public void makeHostMessage(String otherHostname, String ip, int port){
		message = MessageTypes.HOST+" "+otherHostname+" "+ip+" "+Integer.toString(port);
	}
	
	public void makeNeedInfo(){
		message = MessageTypes.NEED_INFO+" "+myhostname;
	}
	
	/**
	 * 
	 * @param state State of the game
	 * @param score A string with hostname + score for every hostname in the game, all in one string
	 */
	public void makeInfo(State state, String score){
		message = MessageTypes.INFO+" "+state+" "+score;
	}
	
	/**
	 * Return the construction of the message as a String
	 */
	public String toString(){
		return message;
	}

}
