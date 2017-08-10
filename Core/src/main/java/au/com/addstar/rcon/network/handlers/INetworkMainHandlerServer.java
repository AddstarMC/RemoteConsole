package au.com.addstar.rcon.network.handlers;

import au.com.addstar.rcon.network.packets.main.PacketInCommand;
import au.com.addstar.rcon.network.packets.main.PacketInPassword;
import au.com.addstar.rcon.network.packets.main.PacketInTabComplete;

public interface INetworkMainHandlerServer extends INetworkHandler
{
	void handleCommand(PacketInCommand packet);
	
	void handleTabComplete(PacketInTabComplete packet);
	
	void handlePassword(PacketInPassword packet);
}
