package au.com.addstar.rcon.network.handlers;

import au.com.addstar.rcon.network.packets.main.PacketInCommand;
import au.com.addstar.rcon.network.packets.main.PacketInPassword;
import au.com.addstar.rcon.network.packets.main.PacketInTabComplete;

public interface INetworkMainHandlerServer extends INetworkHandler
{
	public void handleCommand(PacketInCommand packet);
	
	public void handleTabComplete(PacketInTabComplete packet);
	
	public void handlePassword(PacketInPassword packet);
}
