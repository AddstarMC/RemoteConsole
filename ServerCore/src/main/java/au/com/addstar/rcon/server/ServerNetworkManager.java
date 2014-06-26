package au.com.addstar.rcon.server;

import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;

public class ServerNetworkManager extends NetworkManager
{
	private User mUser;
	public ServerNetworkManager(HandlerCreator creator)
	{
		super(creator);
	}
	
	public void setUser(User user)
	{
		if(user != null)
			throw new IllegalStateException("User already set. Cannot reset user");
		
		mUser = user;
	}
	
	public User getUser()
	{
		return mUser;
	}
}
