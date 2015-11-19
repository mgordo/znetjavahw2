package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import bank.Bank;
import market.*;
import marketthings.*;

public interface ClientInterface extends Remote {

	public void notifyWish(String wishItem) throws RemoteException;
	public String getName() throws RemoteException;
	public Bank getBank() throws RemoteException;
	public void notifySale(String itemName) throws RemoteException;
	
}
