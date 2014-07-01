package au.com.addstar.rcon;

import au.com.addstar.rcon.Event.EventType;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.AbstractNetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerClient;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutTabComplete;

public class NetHandler extends AbstractNetworkHandler implements INetworkMainHandlerClient
{
	private ClientConnection mConnection;
	
	public NetHandler( NetworkManager manager )
	{
		super(manager);
	}
	
	public void setClientConnection(ClientConnection connection)
	{
		mConnection = connection;
	}

	@Override
	public void handleMessage( PacketOutMessage packet )
	{
		ClientMain.getViewManager().addMessage(mConnection, packet.message, packet.type);
		ClientMain.callEvent(new Event(EventType.MessageUpdate, mConnection));
	}

	@Override
	public void handleTabComplete( PacketOutTabComplete packet )
	{
		ClientMain.onTabCompleteDone(packet.results);
	}

}
