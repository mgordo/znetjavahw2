package market;

import java.rmi.Remote;
import java.rmi.RemoteException;
import client.*;
import java.util.List;

import bank.RejectedException;
import marketthings.*;

public interface MarketPlaceInterface extends Remote {
	
	public void registerClient(ClientInterface client) throws RemoteException;
	public List<ItemInterface> showInventory() throws EmptyInventoryException, RemoteException;
	public void buy(String it, ClientInterface client) throws RemoteException, RejectedException;
	public void makeWish(WishInterface wish) throws RemoteException;
	public void sell(ItemInterface it) throws RemoteException;
	public List<WishInterface> showWishes(ClientInterface client) throws RemoteException;
	public void removeClient(ClientInterface client) throws RemoteException;
	
	
	

}
