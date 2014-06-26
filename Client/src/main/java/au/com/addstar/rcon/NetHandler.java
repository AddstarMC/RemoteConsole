package au.com.addstar.rcon;

import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.AbstractNetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerClient;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutTabComplete;

public class NetHandler extends AbstractNetworkHandler implements INetworkMainHandlerClient
{

	public NetHandler( NetworkManager manager )
	{
		super(manager);
	}

	@Override
	public void handleMessage( PacketOutMessage packet )
	{
		ClientMain.printMessage(packet.message);
	}

	@Override
	public void handleTabComplete( PacketOutTabComplete packet )
	{
		// TODO: Probably wake the main thread
	}

}
