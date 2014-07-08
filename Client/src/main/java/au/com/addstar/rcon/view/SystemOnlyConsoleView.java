package au.com.addstar.rcon.view;

import java.util.EnumSet;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.MessageBuffer;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;

public class SystemOnlyConsoleView extends ConsoleView
{
	public SystemOnlyConsoleView()
	{
		super(new MessageBuffer(ClientMain.maxConsoleLines));
	}
	
	@Override
	public String getName()
	{
		return "System";
	}
	
	@Override
	public EnumSet<MessageType> getFilter()
	{
		return EnumSet.of(MessageType.System);
	}
	
	@Override
	public boolean isHandling( ClientConnection connection )
	{
		return false;
	}

}
