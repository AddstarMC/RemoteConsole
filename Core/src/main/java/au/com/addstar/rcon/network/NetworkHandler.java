package au.com.addstar.rcon.network;

import au.com.addstar.rcon.network.packets.PacketInCommand;
import au.com.addstar.rcon.network.packets.PacketOutMessage;

public interface NetworkHandler
{
	public void handleCommand( PacketInCommand command );

	public void handleMessage( PacketOutMessage msg );
}
