package au.com.addstar.rcon.server;

import au.com.addstar.rcon.network.NetworkManager;

public class User
{
	private NetworkManager mManager;
	private String mName;
	
	public User(NetworkManager manager, String name)
	{
		mManager = manager;
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
	
}
