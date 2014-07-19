package au.com.addstar.rcon;

import java.util.ArrayList;
import java.util.EnumSet;

import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.util.Message;

public class MessageBuffer
{
	private ArrayList<Message> mLines;
	private int mNextLine;
	private int mMaxLines;
	private String mOverrideFormat;
	
	public MessageBuffer(int maxLines)
	{
		mLines = new ArrayList<Message>();
		mMaxLines = maxLines;
	}
	
	public void clear()
	{
		mLines.clear();
	}
	
	public String getOverrideFormat()
	{
		return mOverrideFormat;
	}
	
	public void setOverrideFormat(String format)
	{
		mOverrideFormat = format;
	}
	
	private synchronized void addLine(Message message)
	{
		mLines.add(message);
		
		while(mLines.size() > mMaxLines)
		{
			mLines.remove(0);
			
			--mNextLine;
		}
	}
	
	public synchronized boolean isDuplicate(Message message)
	{
		for(int i = mLines.size()-1; i >= 0; i--)
		{
			Message other = mLines.get(i);
			if(other.getTime() < message.getTime() - 500)
				break;
			
			if(message.isRoughDuplicate(other))
				return true;
		}
		
		return false;
	}
	
	public synchronized void addMessage(Message message)
	{
		String[] parts = message.getMessage().split("\n");
		for(String part : parts)
			addLine(message.copyAs(part));
	}
	
	public synchronized void display(ConsoleScreen screen, EnumSet<MessageType> allowed)
	{
		if(mNextLine != 0)
			screen.clear();
		
		mNextLine = 0;
		update(screen, allowed);
	}
	
	public synchronized void update(ConsoleScreen screen, EnumSet<MessageType> allowed)
	{
		for(int i = mNextLine; i < mLines.size(); ++i)
		{
			Message message = mLines.get(i);
			if(!allowed.contains(message.getType()))
				continue;
			
			String format = mOverrideFormat;
			if(format == null)
			{
				ClientConnection connection = ClientMain.getConnectionManager().getConnection(message.getServerId());
				if(connection != null)
					format = connection.getFormat();
				
				if(format == null)
					format = "%m";
			}
			
			String text = message.getFormatted(format);
			if(message.getType() == MessageType.System)
				screen.printErrString(text);
			else
				screen.printString(text);
		}
		
		mNextLine = mLines.size();
	}
	
}
