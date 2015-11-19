package marketthings;

import java.io.Serializable;
import java.rmi.RemoteException;


import client.ClientInterface;

@SuppressWarnings("serial")
public class ItemImpl implements ItemInterface, Serializable{

	private String name;
	private float price;
	private String seller;
	
	public ItemImpl(String name, float price, String seller) throws RemoteException{
		super();
		this.name = name;
		this.price = price;
		this.seller = seller;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSeller() {
		return seller;
	}

	@Override
	public float getPrice() {
		return price;
	}

}
