package au.com.addstar.rcon.network.handlers;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutTabComplete;

public interface INetworkMainHandlerClient extends INetworkHandler
{
	void handleMessage(PacketOutMessage packet);
	
	void handleTabComplete(PacketOutTabComplete packet);
}
