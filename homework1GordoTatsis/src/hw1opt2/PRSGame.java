/**
 * 
 */
package hw1opt2;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import constants.*;
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

import hw1opt2.Broadcast.GameInfo;

public class PRSGame extends JPanel{
	private static final long serialVersionUID = 7503589667461115906L;

	private static PRSGame instance;

	private String playerName = "test";
	private int portTCP;

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
    private Peer myPeer;
    
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
		createGamePanel();
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
	 */
	private void createGamePanel(){
		//TODO: game panel
		
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
	 * Returns to initscreen and produces a custom message, call in case of error in TCPListener
	 * @param errorMessage false if problem in empty, true if filled  
	 */
	
	public void TCPListenerError(final String errorMessage){
		//TODO: actually start TCP message listener, check if it works
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
				JOptionPane.showMessageDialog(frame, "TCP Socket produced an error:\n"+errorMessage, "Error!", JOptionPane.WARNING_MESSAGE);
		    	switchPanel(initPanel);
		    }
	    });
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
	 * Returns the port the server is listening to
	 */
	public static PRSGame getInstance(){
		return instance;	
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
	public void moveMade(String hostname, String move) {
		myPeer.updateMove(hostname, move);
		if(myPeer.allPeersMoved()){
			finishGame();
			//TODO I would imagine here we don't redraw, instead that is done inside finishGame
		}else{
			//TODO call method for redraw if we are showing movements made
		}
		
	}
	

	public void peerIsReady(String hostname) {
		myPeer.setPeerReady(hostname);
		if(myPeer.allPeersReady()){
			//TODO Call some startGame function. Commenting because unsure if already exists
			//startGame();
		}else{
			//TODO redraw if we are showing how many players are ready
		}
		
	}

	public void removePeer(String hostname) {
		myPeer.removePeer(hostname);
		if(myPeer.allPeersMoved()){
			finishGame();
			//TODO I would imagine here we don't redraw, instead that is done inside finishGame
		}else{
			//TODO check if we redraw here
		}
		
	}

	public void putNewPeer(String hostname, String ip, int port) {
		if(myPeer.getMyhostname().equals(hostname)==false){

			try {
				myPeer.putNewPeer(hostname, ip,port);
			} catch (NumberFormatException e) {
				System.out.println("Error converting number");
				e.printStackTrace();
				return;
			} catch (IOException e) {
				System.out.println("Error creating socket for hello message");
				e.printStackTrace();
				return;
			}
		}
		//TODO probably redraw although it may depend on which game state we are at
		
	}

	public void actFast() {
		// TODO Create warning window calling the player to act fast
		
	}

	public void hostAlive(String hostname) {
		// TODO hostname is alive, so restart it timer
		
	}

	public void addHost(String hostname, String ip, int port) {
		try {
			myPeer.putNewPeer(hostname, ip, port);
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

	
	
	
}
