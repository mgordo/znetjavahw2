package marketthings;

import java.io.Serializable;
import java.rmi.RemoteException;


import client.ClientInterface;


@SuppressWarnings("serial")
public class WishImpl implements WishInterface, Serializable {

	private String user;
	private float expectedPrice;
	private String objectName;
	
	public WishImpl(String user, float expected, String name) throws RemoteException{
		super();
		this.user = user;
		expectedPrice = expected;
		objectName = name;
	}
	
	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getItem() {
		return objectName;
	}

	@Override
	public float getWishPrice() {
		return expectedPrice;
	}

}
