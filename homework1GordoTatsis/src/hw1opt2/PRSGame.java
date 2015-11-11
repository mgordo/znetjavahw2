/**
 * 
 */
package hw1opt2;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import constants.*;
import hw1opt2.Broadcast.GameInfo;

public class PRSGame extends JPanel{
	private static final long serialVersionUID = 7503589667461115906L;

	private static PRSGame instance;

	private String playerName = "test";
	private int portTCP;
    private Peer myPeer;

	private static JFrame frame;
	
	private final JPanel initPanel = new JPanel();
    private final JButton createButton = new JButton("Create a Game");
    private final JButton joinButton = new JButton("Join a Game");
    private final JLabel namePromptLabel = new JLabel("Name:");
    private final JTextField nameTextField = new JTextField(playerName);
    private final JLabel portPromptLabel = new JLabel("Port:");
    private final JFormattedTextField portTextField = new JFormattedTextField();
    

    private final JPanel joinPanel = new JPanel();
    private final JPanel directConnectPanel = new JPanel();
    private final JTextField remoteIpTextField = new JTextField();
    private final JFormattedTextField remotePortTextField = new JFormattedTextField();
    private final JButton connectButton = new JButton("Connect");
    private final JLabel availableGamesInfoLabel = new JLabel("Available Games:");
    private final JPanel availableGamesPanel = new JPanel();
    private final JButton refreshButton = new JButton("Refresh Games");
    
    
    private final JPanel gamePanel = new JPanel();
    private final JPanel infoPanel = new JPanel();
    private final JLabel playerLabel = new JLabel();
    private final JLabel scoreLabel = new JLabel("Score:");
    private final JPanel movePanel = new JPanel();
    private final JLabel moveLabel = new JLabel();
    private final JButton rockButton = new JButton("Rock");
    private final JButton paperButton = new JButton("Paper");
    private final JButton scissorsButton = new JButton("Scissors");
    private final JPanel peersPanel = new JPanel();
    private final JButton nextButton = new JButton("Next Round");

    private volatile boolean ready = false;
    private volatile String move = null;
    
    private static DefaultFormatterFactory portFormatterFactory;
    static{
	    NumberFormatter integerFormatter = new NumberFormatter(new DecimalFormat("###"));
	    integerFormatter.setValueClass(Integer.class);
	    integerFormatter.setMinimum(1024);
	    integerFormatter.setMaximum(65535);
	    portFormatterFactory = new DefaultFormatterFactory(integerFormatter);
    }
    
	/**
	 * GUI main method, starts the GUI
	 * @param args
	 */
	public static void main(String[] args) {
		frame = new JFrame("P2P Rock-Paper-Scissors");
        frame.setContentPane(new PRSGame());
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
		Broadcast.init();
	}
	
	/**
	 * PRSGame constructor, initialises the GUI 
	 */
	private PRSGame(){
		createInitPanel();
		createJoinPanel();
		createGamePanel(true);
		add(initPanel);
		instance = this;
		
	}
	
	/**
	 * Creates the initPanel, containing the player's name field and the buttons to create or join a game
	 */
	private void createInitPanel(){

		joinButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!checkInitFields())
					return;
				//TODO: start TCP Listener
				portTCP = Integer.parseInt(portTextField.getText());
				playerName = nameTextField.getText();
				switchPanel(joinPanel);
			}
		});
		
		createButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!checkInitFields())
					return;
				//TODO: start TCP Listener
				portTCP = Integer.parseInt(portTextField.getText());
				playerName = nameTextField.getText();
				Broadcast.setBroadcasting(true);
				switchPanel(gamePanel);
			}
		});
	
		nameTextField.setToolTipText("Enter your player name, without any whitespaces");

	    portTextField.setFormatterFactory(portFormatterFactory);
	    portTextField.setToolTipText("Enter a port to listen to (1024-65535)");
		
		initPanel.setLayout(new GridLayout(3, 2));

		initPanel.add(namePromptLabel);
		initPanel.add(nameTextField);
		initPanel.add(portPromptLabel);
		initPanel.add(portTextField);
		initPanel.add(joinButton);
		initPanel.add(createButton);
	}
	
	/**
	 * Checks the join panel's field, create Dialog if a problem exists
	 * @return false if any field is invalid, true if all are valid  
	 */
	
	private boolean checkInitFields(){
		if (nameTextField.getText().isEmpty()){
			JOptionPane.showMessageDialog(frame, "Name cannot be empty.", "Error!", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		else if (nameTextField.getText().matches("\\s")){
			JOptionPane.showMessageDialog(frame, "Name cannot contain whitespaces.", "Error!", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		else if (portTextField.getText().isEmpty()){
			JOptionPane.showMessageDialog(frame, "Port cannot be empty.", "Error!", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}
	
	
	/**
	 * Creates the joinPanel, containing the available games, and buttons to join them
	 */
	private void createJoinPanel(){
		
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!checkJoinFields())
					return;
				//TODO: connect to game
				switchPanel(gamePanel);
			}
		});
		
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateAvailableGames();
			}
		});

		joinPanel.setLayout(new BoxLayout(joinPanel, BoxLayout.PAGE_AXIS));
		
		directConnectPanel.setLayout(new GridLayout(0, 3));

	    remoteIpTextField.setToolTipText("Enter remote peer's IP address");
	    remotePortTextField.setFormatterFactory(portFormatterFactory);
	    remotePortTextField.setToolTipText("Enter remote peer's port");
	    
	    directConnectPanel.add(remoteIpTextField);
	    directConnectPanel.add(remotePortTextField);
	    directConnectPanel.add(connectButton);
	    
		availableGamesPanel.setLayout(new GridLayout(0, 2));
		availableGamesPanel.add(availableGamesInfoLabel);
		availableGamesPanel.add(refreshButton);
		
	    joinPanel.add(directConnectPanel);
	    joinPanel.add(availableGamesPanel);
	    
	    updateAvailableGames();
	}
	
	/**
	 * Checks the join panel's fields, create Dialog if a problem exists
	 * @return false if any field is invalid, true if all are valid  
	 */
	
	private boolean checkJoinFields(){
		if (remoteIpTextField.getText().isEmpty()){
			JOptionPane.showMessageDialog(frame, "Remote ip cannot be empty.", "Error!", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (remotePortTextField.getText().isEmpty()){
			JOptionPane.showMessageDialog(frame, "Remote port cannot be empty.", "Error!", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}
	
	/**
	 * Updates the availableGamesPanel, containing all peers broadcasting games
	 */
	private void updateAvailableGames(){
		availableGamesPanel.removeAll();
		availableGamesPanel.add(availableGamesInfoLabel);
		availableGamesPanel.add(refreshButton);
		for (GameInfo info : Broadcast.getGames()){
			if (info.name.equals(playerName))
				continue;
		    JLabel gameNameLabel = new JLabel(info.name);
		    JButton connectButton = new JButton("Connect");
		    
		    connectButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//TODO: connect to game
					switchPanel(gamePanel);
					initPeerPanel();
				}
			});

			availableGamesPanel.add(gameNameLabel);
			availableGamesPanel.add(connectButton);
		}
		frame.revalidate();
		frame.pack();
	}
	
	/**
	 * Creates the gamePanel, containing the game information, and move actions
	 * @param first: signifies if this is the initial call
	 */
	private void createGamePanel(boolean first){

		if (first){
			rockButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					executeMove(Move.ROCK);
				}
			});
			
			paperButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					executeMove(Move.PAPER);
				}
			});
			
			scissorsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					executeMove(Move.SCISSORS);
				}
			});
			
			nextButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//TODO: send that we are ready
					ready = true;
					gamePanel.remove(nextButton);
					revalidate();
				}
			});
		}

		ready = false;
		move = null;
		
		infoPanel.setLayout(new GridLayout(0, 2));
		playerLabel.setText(playerName);
	    scoreLabel.setText("Score: "+myPeer.getScore());
		if (first){
			infoPanel.add(playerLabel);
			infoPanel.add(scoreLabel);
		}
		
		if (moveLabel.getParent() != null && moveLabel.getParent() == this)
			remove(moveLabel);
		movePanel.setLayout(new GridLayout(0, 3));
		movePanel.add(rockButton);
		movePanel.add(paperButton);
		movePanel.add(scissorsButton);

		peersPanel.setLayout(new GridLayout(0, 3));

		if (first){
			gamePanel.add(infoPanel);
			gamePanel.add(movePanel);
			gamePanel.add(peersPanel);
			
			initPeerPanel();
		}
		
			    
	}
	
	/**
	 * Initialises the Peer panel with the values contained in the Peer instance
	 */
	private void initPeerPanel(){
		final Map<String,Boolean> readyStates = myPeer.getReady_list();
		final Map<String,Integer> scoreMap = null; //TODO: get score information from other peers
		
		List<String> peers = new ArrayList<String>(readyStates.keySet());
		Collections.sort(peers, new Comparator<String>() {
			public int compare(String left, String right) {
				return scoreMap.get(right)-(scoreMap.get(left));
			}
		});
		
		PeerInfo.clear(peersPanel);
		
		for (String peer : peers)
		    PeerInfo.addPeer(peersPanel, peer, scoreMap.get(peer), readyStates.get(peer));

		revalidate();
		frame.pack();
	}
	
	/**
	 * Returns to initscreen and produces a custom message, call in case of error in TCPListener
	 * @param errorMessage false if problem in empty, true if filled  
	 */
	
	public void TCPListenerError(final String errorMessage){
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
				JOptionPane.showMessageDialog(frame, "TCP Socket produced an error:\n"+errorMessage, "Error!", JOptionPane.WARNING_MESSAGE);
		    	switchPanel(initPanel);
		    }
	    });
	}
	

	/**
	 * Send your move choice to the other peers
	 * @param move: Your move
	 */
	private void executeMove(String move){
		this.move = move;
		movePanel.removeAll();
		moveLabel.setText("You played "+move+".");
		add(moveLabel);
		revalidate();
		frame.pack();
		
		//TODO: send move to peers
	}
	

	
	private void finishGame() {
		// TODO Method to be called when a game ends
		
	}
	
	
	
	/** METHODS FOR UPDATING THE GAME AFTER AN EVENT **/
	/**
	 * Method called when a peer sends its move
	 * @param hostname Name of the host sending the move
	 * @param move Move made
	 */
	public void moveMade(final String name, String move) {
		myPeer.updateMove(name, move);
		if(myPeer.allPeersMoved()){
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
					finishGame();
			    }
		    });
		}
		else{
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			    	PeerInfo.setPeerReady(peersPanel, name, true);
					revalidate();
			    }
		    });
		}
	}
	

	public void peerIsReady(final String name) {
		myPeer.setPeerReady(name);
		if(myPeer.allPeersReady() && ready){
			createGamePanel(false);
			revalidate();
			frame.pack();
		}
		else{
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			    	PeerInfo.setPeerReady(peersPanel, name, true);
					revalidate();
			    }
		    });
		}
	}
	
	
	public void removePeer(final String name) {
		myPeer.removePeer(name);
		if(myPeer.allPeersMoved() && move != null){
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
					PeerInfo.removePeer(peersPanel, name);
					finishGame();
			    }
		    });
		}
		else{
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
					PeerInfo.removePeer(peersPanel, name);
			    }
		    });
		}
		
	}

	public void putNewPeer(final String name, String ip, int port) {
		try {
			myPeer.putNewPeer(name, ip, port);
			//TODO: send our move to the peer if moved
		} catch (NumberFormatException e) {
			System.out.println("Error converting number");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.out.println("Error creating socket for hello message");
			e.printStackTrace();
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
				PeerInfo.addPeer(peersPanel, name, 0, false);
		    }
	    });
		//If we have moved, we send this new peer our own move inmediately
		if(myPeer.getState().equals(State.PLAYING_MOVEMADE)){
			Message movemsg = new Message(myPeer.getMyhostname());
			movemsg.makeSendMoveMessage(myPeer.getMyMove());
			MessageSender.sendMessage(movemsg, name, myPeer.getAddress_list().get(name));
		}
		
	}

	public void actFast() {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
				JOptionPane.showMessageDialog(frame, "Please pick a move, the other players are waiting.", "Hey you!", JOptionPane.INFORMATION_MESSAGE);
		    }
	    });
	}

	public void hostAlive(String hostname) {
		// TODO hostname is alive, so restart it timer
		
	}

	public void addHost(String name, String ip, int port) {
		try {
			myPeer.putNewPeer(name, ip, port);
		} catch (NumberFormatException e) {
			System.out.println("Error converting number");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.out.println("Error creating socket for hello message");
			e.printStackTrace();
			return;
		}
		//TODO probably redraw depending on the state of the game we're at
		
	}
	
	public void arrivedInfo(constants.State state, HashMap<String, Integer> scores) {
		for(String otherpeer: scores.keySet()){
			myPeer.setScoreOfPeer(otherpeer, scores.get(otherpeer));
		}
		myPeer.setState(state);
		//TODO need to redraw after info recieved?
		
	}
	
	/**
	 * Switches visibility to another panel
	 * @param panel: the panel to switch to
	 */
	private void switchPanel(JPanel panel){
		if (panel.getParent() != null && panel.getParent() == this)
			return;
		removeAll();
		add(panel);
		revalidate();
		frame.pack();
	}

	/**
	 * Returns the name set by the player
	 */
	public String getName(){
		return playerName;
	}
	
	/**
	 * Returns the port the server is listening to
	 */
	public int getPort(){
		return portTCP;
	}
	
	/**
	 * Returns the current instance of the PRSGame Class
	 */
	public static PRSGame getInstance(){
		return instance;	
	}
	
	/**
	 * Returns the current instance of the Peer Class
	 */
	public Peer getPeerObject(){
		return myPeer;	
	}
	
	/**
	 * Custom class to hold information about the peersPanel in gamePanel, and allow fast changes
	 */
	private static class PeerInfo{
		private static Map<String,PeerInfo> peers = new HashMap<String,PeerInfo>();
		private JLabel peerNameLabel;
		private JLabel peerScoreLabel;
		private JLabel peerReadyLabel;
		
		private PeerInfo(JPanel panel, String name, int score, boolean ready){
		    peerNameLabel = new JLabel(name);
		    peerScoreLabel = new JLabel(Integer.toString(score));
		    peerReadyLabel = new JLabel(ready?"Ready":"Not Ready");
		}
		
		public static void addPeer(JPanel panel, String name, int score, boolean ready){
			PeerInfo peerInfo = new PeerInfo(panel, name, score, ready);
			
		    panel.add(peerInfo.peerNameLabel);
		    panel.add(peerInfo.peerScoreLabel);
		    panel.add(peerInfo.peerReadyLabel);
		    
		    peers.put(name, peerInfo);
		}
		
		public static void setPeerReady(JPanel panel, String name, boolean ready){
			peers.get(name).peerReadyLabel.setText(ready?"Ready":"Not Ready");
		}
		
		public static void removePeer(JPanel panel, String name){
			PeerInfo peerInfo = peers.get(name);
					
			panel.remove(peerInfo.peerNameLabel);
		    panel.remove(peerInfo.peerScoreLabel);
		    panel.remove(peerInfo.peerReadyLabel);
		    
		    peers.remove(name);
		}
		
		public static void clear(JPanel panel){
			for (String name: peers.keySet())
				removePeer(panel, name);
		}
	}

	

}
