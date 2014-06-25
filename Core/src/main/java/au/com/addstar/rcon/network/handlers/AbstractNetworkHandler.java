package au.com.addstar.rcon.network.handlers;

import au.com.addstar.rcon.network.NetworkManager;


public abstract class AbstractNetworkHandler implements INetworkHandler
{
	private NetworkManager mManager;
	
	public AbstractNetworkHandler(NetworkManager manager)
	{
		mManager = manager;
	}
	
	public final NetworkManager getManager()
	{
		return mManager;
	}
	
	protected void disconnect(String reason)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
