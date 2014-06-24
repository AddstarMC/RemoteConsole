package au.com.addstar.rcon;

import au.com.addstar.rcon.network.NetworkHandler;
import au.com.addstar.rcon.network.packets.PacketInCommand;
import au.com.addstar.rcon.network.packets.PacketOutMessage;

public class NetHandler implements NetworkHandler
{

	@Override
	public void handleCommand( PacketInCommand command )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void handleMessage( PacketOutMessage msg )
	{
	}

}
