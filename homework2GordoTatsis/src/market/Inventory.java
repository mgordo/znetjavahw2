package market;

import java.rmi.RemoteException;
import java.util.*;

import client.ClientInterface;
import marketthings.ItemInterface;
import marketthings.WishInterface;

/**
 * 
 * This class implements the inventory for a given Market
 *
 */
public class Inventory implements InventoryInterface {

	private ArrayList<WishInterface> wishes;
	private ArrayList<ItemInterface> items;
	private ArrayList<ClientInterface> clients;
	
	
	public Inventory(){
		wishes = new ArrayList<WishInterface>();
		items = new ArrayList<ItemInterface>();
		clients = new ArrayList<ClientInterface>();
	}
	
	/**
	 * Returns all the items on sale
	 */
	@Override
	public synchronized List<ItemInterface> getAllItems() {
		return items;
	}

	/**
	 * This function gets the wishes of a client
	 * @return A List with all the wishes, empty list otherwise
	 */
	@Override
	public synchronized List<WishInterface> getWishes(String client) {
		ArrayList<WishInterface> results = new ArrayList<WishInterface>();
		for(WishInterface wish: wishes){
			if(wish.getUser().equals(client)){
				results.add(wish);
			}
		}
		return results;
	}

	/**
	 * This method removes an item from the inventory
	 * @param item is the item to be removed
	 */
	@Override
	public synchronized void removeItem(ItemInterface item) {
		if(items.contains(item)){
			items.remove(item);
		}
		System.out.println("Item "+item.getName()+" succesfully removed from the market");

	}

	/**
	 * This method adds a new item to the inventory
	 * @param item is the new item to be added
	 */
	@Override
	public synchronized void  addItem(ItemInterface item) {
		items.add(item);
		System.out.println("Item "+item.getName()+" added succesfully to the market");

	}

	/**
	 * This method removes a wish from the inventory
	 * @param wish is the Wish object to be removed
	 */
	@Override
	public synchronized void removeWish(WishInterface wish) {
		WishInterface result = null;
		for(WishInterface otherwish : wishes){
			if(otherwish.getItem().equals(wish.getItem()) && otherwish.getUser().equals(wish.getUser())){
				result = otherwish;
				break;
			}
		}
		if(result!=null) wishes.remove(result);
		
		System.out.println("Wish for "+wish.getItem()+" was succesfully removed");
	}

	/**
	 * This method adds a wish in the inventory
	 * @param wish is the wish object to be added
	 */
	@Override
	public synchronized void addWish(WishInterface wish) {
		
		for(WishInterface otherwish : wishes){
			if(otherwish.getItem().equals(wish.getItem()) && otherwish.getUser().equals(wish.getUser())){
				return;
			}
		}
		
		wishes.add(wish);
		System.out.println("Wish for "+wish.getItem()+" for value of "+wish.getWishPrice()+" was succesfully added");
	}

	
	@Override
	public synchronized boolean isItemAvailable(String item) {
		for(ItemInterface otheritems: items){
			if(otheritems.getName().equals(item)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a client to the inventory as a registered client
	 * @paran client is the client object to be registered
	 * @throws RemoteException in case registration was unsusccesful
	 */
	@Override
	public void addClient(ClientInterface client) throws RemoteException {
		
		if(clients.contains(client)){
			throw new RemoteException("Error, there is a user with the same name");
		}
		clients.add(client);
		
		
		System.out.println("The client "+client.getName()+" was succesfully added to the market");
	}

	/**
	 * This method removes a client and deletes all his wishes and items from the marketplace
	 * @param client is the client to be removed
	 * @throws RemoteException if the removal could not be completed
	 */
	@Override
	public void removeClient(ClientInterface client) throws RemoteException {
		
		if(clients.contains(client.getName())){
			clients.remove(client.getName());
		}
		for (WishInterface otherwish: wishes){
			if (otherwish.getUser().equals(client.getName())){
				wishes.remove(otherwish);
			}
		}
		for (ItemInterface otheritem : items){
			if(otheritem.getSeller().equals(client.getName())){
				items.remove(otheritem);
			}
		}
		
		System.out.println("Client "+client.getName()+" was removed from the market");
		
	}

	@Override
	public boolean isClientRegistered(ClientInterface client) throws RemoteException {
		for(ClientInterface otherclient : clients){
			if(otherclient.getName().equals(client.getName())){
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemInterface getItem(String item) {
		for(ItemInterface otheritem : items){
			if(otheritem.getName().equals(item)){
				return otheritem;
			}
		}
		return null;
	}

	@Override
	public WishInterface searchWish(String itemName, float price, String user) {
		for(WishInterface otherwish : wishes){
			if(otherwish.getItem().equals(itemName) && otherwish.getWishPrice()>=price && otherwish.getUser().equals(user)){
				return otherwish;
			}
		}
		return null;
	}

	@Override
	public List<WishInterface> searchWish(String itemName, float price) {
		ArrayList<WishInterface> results = new ArrayList<WishInterface>();
		for(WishInterface otherwish : wishes){
			if(otherwish.getItem().equals(itemName) && otherwish.getWishPrice()>=price){
				results.add(otherwish);
			}
		}
		if(results.isEmpty()) return null;
		return results;
	}

	@Override
	public ClientInterface getClient(String user) {
		for(ClientInterface client: clients){
			try {
				if(client.getName().equals(user)){
					return client;
				}
			} catch (RemoteException e) {
				System.out.println("Could not retrieve remote name when looking for client");
				return null;
			}
		}
		return null;
	}

}
