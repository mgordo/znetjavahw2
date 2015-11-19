package market;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import bank.Account;
import bank.Bank;
import bank.RejectedException;
import client.ClientImpl;
import client.ClientInterface;
import marketthings.ItemInterface;
import marketthings.WishInterface;
/**
 * This class implements MarketInterface, and provides the necessary methods to act as the Market described in HW2
 * @author Miguel
 *
 */
@SuppressWarnings("serial")
public class MarketPlaceImpl extends UnicastRemoteObject implements MarketPlaceInterface {

	private InventoryInterface inventory;
	private static final int REGISTRY_PORT_NUMBER = 1099;
	
	/**
	 * Constructor for the class. Registers the new object in RMI Naming
	 * @throws RemoteException
	 */
	public MarketPlaceImpl() throws RemoteException{
		super();
		inventory = new Inventory();
		try{
			try {
				LocateRegistry.getRegistry(REGISTRY_PORT_NUMBER).list();
			} catch (RemoteException e) {
				LocateRegistry.createRegistry(REGISTRY_PORT_NUMBER);
			}
			Naming.rebind("rmi://localhost/Market", this);
			System.out.println("The Market is ready.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
		
	/**
	 * To be called when a client wishes to register in this marketplace
	 * @param client Client object that wishes to register
	 */
	@Override
	public void registerClient(ClientInterface client) throws RemoteException {
		inventory.addClient(client);

	}

	/**
	 * To be called when a client wishes to check the inventory of the market
	 */
	@Override
	public List<ItemInterface> showInventory() throws EmptyInventoryException {
		List<ItemInterface> result = inventory.getAllItems();
		if(result.isEmpty()){
			throw new EmptyInventoryException("No items on sale!");
		}
		return result;
	}

	/**
	 * This method should be called when client wishes to buy Item item. It will 
	 * arrange payment with the client's bank. 
	 * @param item Name of the item to be bought
	 * @param client Client who wishes to purchase param item
	 */
	@Override
	public void buy(String item, ClientInterface client) throws RemoteException, RejectedException {
		if(!inventory.isClientRegistered(client)){
			throw new RemoteException("You are not registered in the MarketPlace!");
		}
		if(!inventory.isItemAvailable(item)){
			throw new RemoteException("The item is not available in the marketplace");
		}
		ItemInterface marketItem = inventory.getItem(item);
		if(marketItem == null){
			throw new RemoteException("A big mistake has happened. The item is in the market but was not retrieved");
		}
		if(marketItem.getSeller().equals(client.getName())){
			throw new RemoteException("You cannot buy your own item!");
		}
		Bank localbank = client.getBank();
		Bank bank;
		try {
			bank = (Bank) Naming.lookup( localbank.getBankName());
		} catch (MalformedURLException | NotBoundException e) {
			throw new RemoteException("The name of your bank could not be found");
		}
		
		Account account = bank.getAccount(client.getName());
		account.withdraw(marketItem.getPrice());
		inventory.removeItem(marketItem);
		//Remove wish if any
		WishInterface possibleWish = inventory.searchWish(marketItem.getName(), marketItem.getPrice(),client.getName());
		if(possibleWish!=null){
			if(possibleWish.getUser().equals(client.getName())){
				inventory.removeWish(possibleWish);
			}
		}
		//notify seller
		ClientInterface seller = inventory.getClient(marketItem.getSeller());
		seller.notifySale(marketItem.getName());
		
	}

	/**
	 * This method adds a wish to the market
	 * @param wish Wish object that is to be included in the market
	 */
	@Override
	public void makeWish(WishInterface wish) throws RemoteException {
		inventory.addWish(wish);

	}

	/**
	 * This method is to be called when a client wishes to put an item on sale
	 * @param it is the Item to be put on sale
	 */
	@Override
	public void sell(ItemInterface it) throws RemoteException {
		inventory.addItem(it);
		List<WishInterface> wishlist = inventory.searchWish(it.getName(), it.getPrice());
		if(wishlist!=null){
			
			for(WishInterface wish: wishlist){
				ClientInterface clientobj;
				clientobj = inventory.getClient(wish.getUser());
				clientobj.notifyWish(wish.getItem());
			}
			
		}
	}

	/**
	 * This method returns a list of wishes for a given client
	 * @param client Client object whose wishes are to be returned
	 * @throws RemoteException if the list was empty
	 * @return List with wishes, if list is empty an exception is raised
	 */
	@Override
	public List<WishInterface> showWishes(ClientInterface client) throws RemoteException {
		List<WishInterface> results = inventory.getWishes(client.getName());
		if(results==null){
			throw new RemoteException("You have not made any wishes");
		}
		return results;
		
	}

	public static void main(String[] args) {
		
		
		try {
			MarketPlaceImpl market = new MarketPlaceImpl();
		} catch (RemoteException e) {
			System.out.println("Could not create a new MarketPlace!");
			e.printStackTrace();
			return;
		}
		
		

	}



	/**
	 * This method removes a client from the marketplace, deletes his wishes and his sales
	 * @param client Client object to be removed
	 * @throws RemoteException if the client, client sales or client wishes could not be removed
	 */
	@Override
	public void removeClient(ClientInterface client) throws RemoteException {
		ArrayList<ItemInterface> items = new ArrayList<ItemInterface>();
		for(ItemInterface it: inventory.getAllItems()){
			if(it.getSeller().equals(client.getName())){
				items.add(it);
			}
		}
		if(!items.isEmpty()){
			
			for(ItemInterface it : items){
				inventory.removeItem(it);
			}
		}
		
		for(WishInterface it: inventory.getWishes(client.getName())){
			if(it.getUser().equals(client.getName())){
				inventory.removeWish(it);
			}
		}
		inventory.removeClient(client);
				
	}

}
