package au.com.addstar.rcon.network.handlers;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutTabComplete;

public interface INetworkMainHandlerClient extends INetworkHandler
{
	public void handleMessage(PacketOutMessage packet);
	
	public void handleTabComplete(PacketOutTabComplete packet);
}
