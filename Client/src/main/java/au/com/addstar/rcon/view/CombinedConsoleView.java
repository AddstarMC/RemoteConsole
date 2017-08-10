package au.com.addstar.rcon.view;

import java.util.HashSet;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.MessageBuffer;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;

public class CombinedConsoleView extends ConsoleView
{
	private HashSet<ClientConnection> mConnections;
	
	public CombinedConsoleView()
	{
		super(new MessageBuffer(ClientMain.maxConsoleLines));
		mConnections = new HashSet<>();
	}
	
	public void addConnection(ClientConnection connection)
	{
		mConnections.add(connection);
	}
	
	public void removeConnection(ClientConnection connection)
	{
		mConnections.remove(connection);
	}
	
	@Override
	public String getPrefix( ClientConnection connection, MessageType type )
	{
		return String.format("[%s] ", connection.getId());
	}
	
	@Override
	public boolean isHandling( ClientConnection connection )
	{
		return mConnections.contains(connection);
	}
}
