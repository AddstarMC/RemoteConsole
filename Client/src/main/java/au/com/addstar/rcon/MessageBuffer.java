package au.com.addstar.rcon;

import java.util.ArrayList;
import java.util.EnumSet;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;

public class MessageBuffer
{
	private ArrayList<String> mLines;
	private ArrayList<MessageType> mLineTypes;
	private int mNextLine;
	private int mMaxLines;
	
	public MessageBuffer(int maxLines)
	{
		mLines = new ArrayList<String>();
		mLineTypes = new ArrayList<MessageType>();
		mMaxLines = maxLines;
	}
	
	public void clear()
	{
		mLines.clear();
		mLineTypes.clear();
	}
	
	private synchronized void addLine(String line, MessageType type)
	{
		mLines.add(line);
		mLineTypes.add(type);
		
		while(mLines.size() > mMaxLines)
		{
			mLines.remove(0);
			mLineTypes.remove(0);
			
			--mNextLine;
		}
	}
	
	public synchronized void addMessage(String message, MessageType type)
	{
		String[] parts = message.split("\n");
		for(String part : parts)
			addLine(part, type);
	}
	
	public synchronized void display(ConsoleScreen screen, EnumSet<MessageType> allowed)
	{
		if(mNextLine != 0)
			screen.clear();
		
		for(int i = 0; i < mLines.size(); ++i)
		{
			if(!allowed.contains(mLineTypes.get(i)))
				continue;
			
			screen.printString(mLines.get(i));
		}
		
		mNextLine = mLines.size();
	}
	
	public synchronized void update(ConsoleScreen screen, EnumSet<MessageType> allowed)
	{
		for(int i = mNextLine; i < mLines.size(); ++i)
		{
			if(!allowed.contains(mLineTypes.get(i)))
				continue;
			
			screen.printString(mLines.get(i));
		}
		
		mNextLine = mLines.size();
	}
	
}
