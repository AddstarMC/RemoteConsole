package au.com.addstar.rcon.view;

import java.util.EnumSet;

import au.com.addstar.rcon.MessageBuffer;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.util.Message;

public abstract class ConsoleView
{
	private EnumSet<MessageType> mFilter;
	private final MessageBuffer mBuffer;
	private String mName;

	protected ConsoleView(MessageBuffer buffer)
	{
		mFilter = EnumSet.allOf(MessageType.class);
		mBuffer = buffer;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setName(String name)
	{
		mName = name;
	}
	
	public EnumSet<MessageType> getFilter()
	{
		return mFilter;
	}

	public void setFilter( EnumSet<MessageType> filter )
	{
		mFilter = filter;
	}
	
	public MessageBuffer getBuffer()
	{
		return mBuffer;
	}
	
	public String getPrefix(ClientConnection connection, MessageType type)
	{ 
		return "";
	}
	
	public String getSuffix(ClientConnection connection, MessageType type)
	{ 
		return "";
	}
	
	public void addMessage(Message message)
	{
		mBuffer.addMessage(message);
	}
	
	public abstract boolean isHandling(ClientConnection connection);
}