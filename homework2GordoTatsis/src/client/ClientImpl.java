package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import bank.Account;
import bank.Bank;
import bank.RejectedException;
import market.EmptyInventoryException;
import market.MarketPlaceInterface;
import marketthings.ItemImpl;
import marketthings.ItemInterface;
import marketthings.WishImpl;
import marketthings.WishInterface;

/**
 * This class implements ClientInterface, and extends RemoteObject. Contains all the functionality for the client to register in the marketplace
 * @author Nikolaos, Miguel
 *
 */
@SuppressWarnings("serial")
public class ClientImpl extends UnicastRemoteObject implements ClientInterface{

	private Bank myBank;
	private String myBankName = "Nordea";
	private String name;
	private Account myAccount;
	private static final String USAGE = "Enter your command: exit | inventory | wish [item] [price] | sell [item] [price]\n";
	private static final int REGISTRY_PORT_NUMBER = 1099;
	
	private enum Commands{
		EXIT,INVENTORY,WISH,BUY,SELL,UNKNOWN
	}
	
	
	/**
	 * Constructor for the class. Will remotely register client according to param name
	 * @param name Name of the client to register
	 * @throws RemoteException
	 */
	public ClientImpl(String name) throws RemoteException{
		super();
		this.name = name;
		myAccount = null;
		try {
			myBank = (Bank) Naming.lookup(myBankName);
		} catch (MalformedURLException | NotBoundException e) {
			System.out.println("Could not retrieve remote bank");
			e.printStackTrace();
		}
		
		try{
			try {
				LocateRegistry.getRegistry(REGISTRY_PORT_NUMBER).list();
			} catch (RemoteException e) {
				LocateRegistry.createRegistry(REGISTRY_PORT_NUMBER);
			}
			Naming.rebind("rmi://localhost/"+name, this);
			System.out.println("Client "+name+" has been registered remotely");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * This function should be called when the marketplace wants to inform the client of an item that 
	 * @param wishItem String with the name of the item that was wished before by this clientImpl
	 */
	@Override
	public void notifyWish(String wishItem) {
		System.out.println("Your wish for "+wishItem+" has been granted! Visit Market to buy");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Bank getBank() {
		return myBank;
	}

	public static void main(String[] args) {
		
		ClientImpl client;
		MarketPlaceInterface marketInt;
		if(args.length!=2){
			System.out.println("USAGE: name initial_amount");
			return;
		}
		
		
		try {
			client= new ClientImpl(args[0] );
		} catch (RemoteException e1) {
			e1.getCause();
			return;
		}
		
		try {
			marketInt = (MarketPlaceInterface) Naming.lookup("Market");
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println("Could not retrieve remote Market!");
			e.printStackTrace();
			return;
		}
		
		//Register in market and create account in bank
		try {
			String[] listaccounts = client.getBank().listAccounts();
			if(listaccounts!=null){
				for(String s: listaccounts){
					if(s!=null){
						if(s.equals(client.getName())){
							client.setMyAccount(client.getBank().getAccount(client.getName()));
						}
					}
					
				}
			}
			if(client.getMyAccount()==null) client.setMyAccount(client.getBank().newAccount(client.getName()));
		} catch (RemoteException | RejectedException e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
			return;
		}
		//Deposit money in the account, indicated in the arguments used when running
		try {
			client.getMyAccount().deposit(Float.parseFloat(args[1]));
		} catch (NumberFormatException | RemoteException | RejectedException e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
			return;
		}
		
		try {
			marketInt.registerClient(client);
		} catch (RemoteException e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
			return;
		}
		System.out.println("Succesfully registered in the marketplace");
		BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
		String input=null;
		boolean main=true;
		Command cmd;
		//Add shutdown hook should the program close unexpectedly
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	try {
					marketInt.removeClient((ClientInterface)client);
				} catch (RemoteException e) {
					System.out.println("When closing, unable to remove from the Market");
				}
				System.out.println("Succesfully unregistered from the marketplace upon exit");
				try {
					LocateRegistry.getRegistry(REGISTRY_PORT_NUMBER).list();
					
				} catch (RemoteException e) {
					try {
						LocateRegistry.createRegistry(REGISTRY_PORT_NUMBER);
					} catch (RemoteException e1) {
						System.out.println("Could not locate registry for disconnection");
						e1.printStackTrace();
					}
				}
				try {
					Naming.unbind("rmi://localhost/"+client.getName());
				} catch (RemoteException | MalformedURLException | NotBoundException e) {
					System.out.println("Could not unbind client");
					e.printStackTrace();
				}
				System.out.println("RMI unbinding successful");
		    }
		 });
		//Main loop
		while(main){
			System.out.println(USAGE);
			try {
				input = consoleIn.readLine();
				cmd = parse(input);
				
				switch(cmd.getCommand()){
				case BUY:
					try {
						marketInt.buy(cmd.getItem(),client);
					} catch (RejectedException | RemoteException e) {
						System.out.println(e.getMessage());
					}
					System.out.println("Success! You bought "+cmd.getItem()+"");
					break;
				case EXIT:
					main=false;
					marketInt.removeClient((ClientInterface)client);
					System.out.println("Succesfully unregistered from the marketplace upon exit");
					break;
				case INVENTORY:
					List<ItemInterface> inv = null;
					try{
						inv = marketInt.showInventory();
					}catch(EmptyInventoryException e){
						System.out.println("The market is empty. No items on sale!");
						break;
					}
					System.out.println("Name of Item | Price | Seller");
					for(ItemInterface it : inv){
						System.out.println(it.getName()+" "+it.getPrice()+"SEK "+it.getSeller());
					}
					break;
				case SELL:
					ItemImpl item = new ItemImpl(cmd.getItem(),cmd.getPrice(),client.getName());
					marketInt.sell(item);
					System.out.println("Item succesfully placed on the market");
					break;
				case UNKNOWN:
					System.out.println("The command you entered is not valid");
					break;
				case WISH:
					WishImpl wish = new WishImpl(client.getName(),cmd.getPrice(),cmd.getItem());
					marketInt.makeWish(wish);
					System.out.println("Success! Your wish for "+wish.getItem()+" has been placed");
					break;
				default:
					System.out.println("The command you entered is not valid");
					break;
				
				}
				
				
			} catch (IOException e) {
				System.out.println("Error reading command line");
				e.printStackTrace();
				return;
			}
			
		}
		
		
		
		try {
			LocateRegistry.getRegistry(REGISTRY_PORT_NUMBER).list();
			
		} catch (RemoteException e) {
			try {
				LocateRegistry.createRegistry(REGISTRY_PORT_NUMBER);
			} catch (RemoteException e1) {
				System.out.println("Could not locate registry for disconnection");
				e1.printStackTrace();
			}
		}
		try {
			Naming.unbind("rmi://localhost/"+client.getName());
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			System.out.println("Could not unbind client");
			e.printStackTrace();
		}
		
		
		
		System.out.println("Exiting client. See you soon!");
		System.exit(0);

	}
	/**
	 * private method used for parsing user input
	 * @param input input from the user
	 * @return Command with the parameters parsed
	 */
	private static Command parse(String input) {
		
		input = input.toUpperCase();
		
		String[] words = input.split(" ");
		Commands number = Commands.UNKNOWN;
		try{
			number = Commands.valueOf(words[0]);
		}catch(IllegalArgumentException e){
			number = Commands.UNKNOWN;
		}
		switch(number){
			
		case BUY:
			if (words.length>=2){
				return new Command(Commands.BUY,words[1],0);
			}
			return new Command(Commands.UNKNOWN,"UNKNOWN",0);
			
		case EXIT:
			return new Command(Commands.EXIT,"UNKNOWN",0);
			
		case INVENTORY:
			return new Command(Commands.INVENTORY,"UNKNOWN",0);
			
		case SELL:
			if(words.length>=3){
				try{
					return new Command(Commands.SELL,words[1],Float.valueOf(words[2]));
				}catch(NumberFormatException e){
					return new Command(Commands.INVENTORY,"UNKNOWN",0);
				}
				
			}
			return new Command(Commands.UNKNOWN,"UNKNOWN",0);
			
		case WISH:
			if(words.length>=3){
				
				try{
					return new Command(Commands.WISH,words[1],Float.valueOf(words[2]));
				}catch(NumberFormatException e){
					return new Command(Commands.INVENTORY,"UNKNOWN",0);
				}
				
				
			}
			return new Command(Commands.UNKNOWN,"UNKNOWN",0);
			
		default:
			return new Command(Commands.UNKNOWN,"UNKNOWN",0);
			
				
		}
		
	}
	public Account getMyAccount() {
		return myAccount;
	}
	public void setMyAccount(Account myAccount) {
		this.myAccount = myAccount;
	}
	
	/**
	 * Private class that models a command input by the user
	 * 
	 *
	 */
	private static class Command{
		
		private Commands command;
		private String item;
		private float price;
		
		public Command(Commands com, String it, float price){
			this.command = com;
			this.item = it;
			this.price = price;
		}

		public Commands getCommand(){
			return command;
		}
		
		public String getItem(){
			return item;
		}
		
		public float getPrice(){
			return price;
		}
		
	}

	/**
	 * This method should be called when the market wishes to notify the client of a successful sale
	 * @param itemName Name of the item sold, and being notified to the client
	 */
	@Override
	public void notifySale(String itemName) throws RemoteException {
		System.out.println("The item "+itemName+" was sold in the market! Funds were added to your account");
		
	}
	
}
