/**
 * 
 */
package hw1opt2;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import hw1opt2.Broadcast.GameInfo;

public class PRSGame extends JPanel{
	private static PRSGame instance;

	private String playerName = "";
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
    private final JTextField remoteIpTextField = new JTextField();
    private final JFormattedTextField remotePortTextField = new JFormattedTextField();
    private final JButton connectButton = new JButton("Connect");
    private final JPanel availableGamesPanel = new JPanel();
    private final JButton refreshButton = new JButton("Refresh Games");
    
    
    private final JPanel gamePanel = new JPanel();
    
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
				if (!startTCPListener())
					return;
				playerName = nameTextField.getText();
				removeAll();
				add(joinPanel);
				revalidate();
			}
		});
		
		createButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!checkInitFields())
					return;
				if (!startTCPListener())
					return;
				playerName = nameTextField.getText();
				Broadcast.setBroadcasting(true);
				removeAll();
				add(gamePanel);
				revalidate();
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
	 * Starts TCP listener, creates Dialog if a problem exists (like invalid port number)
	 * @return false if problem in empty, true if filled  
	 */
	
	private boolean startTCPListener(){
		//TODO: actually start TCP message listener, check if it works
		if (false){
			JOptionPane.showMessageDialog(frame, "Cannot start listener on the specified port.", "Error!", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		portTCP = Integer.parseInt(portTextField.getText());
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
				removeAll();
				add(gamePanel);
				invalidate();
			}
		});
		
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateAvailableGames();
			}
		});

		joinPanel.setLayout(new GridLayout(0, 2));
		availableGamesPanel.setLayout(new GridLayout(0, 2));

	    remoteIpTextField.setToolTipText("Enter remote peer's ip");
	    
		remotePortTextField.setFormatterFactory(portFormatterFactory);
	    remotePortTextField.setToolTipText("Enter remote peer's port");

	    joinPanel.add(remoteIpTextField);
	    joinPanel.add(remotePortTextField);
	    joinPanel.add(connectButton);
	    joinPanel.add(availableGamesPanel);
		joinPanel.add(refreshButton);
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
		for (GameInfo info : Broadcast.getGames()){
		    JLabel gameNameLabel = new JLabel(info.name);
		    JButton connectButton = new JButton("Connect");
		    
		    connectButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//TODO: connect to game
					removeAll();
					add(gamePanel);
				}
			});

			availableGamesPanel.add(gameNameLabel);
			availableGamesPanel.add(connectButton);
		}
	}
	
	/**
	 * Creates the gamePanel, containing the game information, and move actions
	 */
	private void createGamePanel(){
		//TODO: game panel
		
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
}
