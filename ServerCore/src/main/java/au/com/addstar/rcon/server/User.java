package au.com.addstar.rcon.server;

import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.server.auth.StoredPassword;

public abstract class User
{
	private NetworkManager mManager;
	private String mName;
	
	public User(String name)
	{
		mName = name;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public NetworkManager getManager()
	{
		return mManager;
	}
	
	public void setManager(NetworkManager manager)
	{
		mManager = manager;
	}
	
	public abstract StoredPassword getPassword();
}
