
package au.com.addstar.rcon;

import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.packets.PacketInCommand;
import au.com.addstar.rcon.server.AbstractServerHandler;

public class NetHandler extends AbstractServerHandler
{
	public NetHandler(NetworkManager manager)
	{
		super(manager);
	}
	
	@Override
	public void handleCommand( PacketInCommand command )
	{
		System.out.println("Command: " + command);
	}
}
