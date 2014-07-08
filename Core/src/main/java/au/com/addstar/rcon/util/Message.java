package au.com.addstar.rcon.util;

import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;

public class Message
{
	private String mMessage;
	private long mTime;
	private Level mLevel;
	private String mThread;
	private String mLogger;
	private MessageType mType;
	
	public Message(String message, MessageType type, long time, Level level, String thread, String logger)
	{
		mMessage = message;
		mType = type;
		mTime = time;
		mLevel = level;
		mThread = thread;
		mLogger = logger;
	}
	
	public Message(String message, MessageType type, String logger)
	{
		this(message, type, System.currentTimeMillis(), Level.INFO, Thread.currentThread().getName(), "");
	}
	
	public String getMessage()
	{
		return mMessage;
	}
	
	public String getThreadName()
	{
		return mThread;
	}
	
	public long getTime()
	{
		return mTime;
	}
	
	public MessageType getType()
	{
		return mType;
	}
	
	public Level getLevel()
	{
		return mLevel;
	}
	
	public String getLogger()
	{
		return mLogger;
	}
	
	private static Pattern mPattern = Pattern.compile("%(?:(message|msg|m)|(level|p)|(thread|t)|(?:date|d)\\{(.*?)\\}|(server)|(serverid|sid)|(n))");
	
	public String getFormatted(String format, String serverName, String serverId)
	{
		Matcher matcher = mPattern.matcher(format);
		
		StringBuffer buffer = new StringBuffer();
		while(matcher.find())
		{
			if(matcher.group(1) != null) // Message
				matcher.appendReplacement(buffer, mMessage);
			else if(matcher.group(2) != null) // Level
				matcher.appendReplacement(buffer, mLevel.getLocalizedName());
			else if(matcher.group(3) != null) // Thread
				matcher.appendReplacement(buffer, mThread);
			else if(matcher.group(4) != null) // Date
				matcher.appendReplacement(buffer, new SimpleDateFormat(matcher.group(4)).format(mTime));
			else if(matcher.group(5) != null) // ServerName
				matcher.appendReplacement(buffer, serverName);
			else if(matcher.group(6) != null) // ServerId
				matcher.appendReplacement(buffer, serverId);
			else if(matcher.group(6) != null) // Newline
				matcher.appendReplacement(buffer, ""); // Ignoring as newline is already present
		}
		
		matcher.appendTail(buffer);
		return buffer.toString();
	}
}
