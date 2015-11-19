package market;
import java.rmi.RemoteException;
import java.util.List;
import client.*;
import marketthings.*;
public interface InventoryInterface {

	List<ItemInterface> getAllItems();
	List<WishInterface> getWishes(String client);
	void removeItem(ItemInterface item);
	void addItem(ItemInterface item);
	void removeWish(WishInterface wish);
	void addWish(WishInterface wish);
	boolean isItemAvailable(String item);
	void addClient(ClientInterface client) throws RemoteException;
	void removeClient(ClientInterface client) throws RemoteException;
	boolean isClientRegistered(ClientInterface client) throws RemoteException;
	ItemInterface getItem(String item);
	List<WishInterface> searchWish(String itemName,float price);
	WishInterface searchWish(String itemName,float price,String user);
	ClientInterface getClient(String user);
	
}
