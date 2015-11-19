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

@SuppressWarnings("serial")
public class MarketPlaceImpl extends UnicastRemoteObject implements MarketPlaceInterface {

	private InventoryInterface inventory;
	private static final int REGISTRY_PORT_NUMBER = 1099;
	
	
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
	
		
	
	@Override
	public void registerClient(ClientInterface client) throws RemoteException {
		inventory.addClient(client);

	}

	@Override
	public List<ItemInterface> showInventory() throws EmptyInventoryException {
		List<ItemInterface> result = inventory.getAllItems();
		if(result.isEmpty()){
			throw new EmptyInventoryException("No items on sale!");
		}
		return result;
	}

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

	@Override
	public void makeWish(WishInterface wish) throws RemoteException {
		inventory.addWish(wish);

	}

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
