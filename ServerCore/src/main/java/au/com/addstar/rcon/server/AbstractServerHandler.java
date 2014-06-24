package au.com.addstar.rcon.server;

import au.com.addstar.rcon.network.NetworkHandler;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.packets.PacketInCommand;
import au.com.addstar.rcon.network.packets.PacketOutMessage;

public abstract class AbstractServerHandler implements NetworkHandler
{
	private NetworkManager mManager;
	
	public AbstractServerHandler(NetworkManager manager)
	{
		mManager = manager;
	}
	
	public final NetworkManager getManager()
	{
		return mManager;
	}
	
	public abstract void handleCommand( PacketInCommand command );

	@Override
	public void handleMessage( PacketOutMessage msg )
	{
		throw new UnsupportedOperationException();
	}

}
