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
	private String mServerName;
	private String mServerId;
	
	public Message(String message, MessageType type, long time, Level level, String thread, String logger, String serverId, String serverName)
	{
		mMessage = message;
		mType = type;
		mTime = time;
		mLevel = level;
		mThread = thread;
		mLogger = logger;
		mServerId = serverId;
		mServerName = serverName;
	}
	
	public Message(String message, MessageType type, long time, Level level, String thread, String logger)
	{
		this(message, type, time, level, thread, logger, null, null);
	}
	
	public Message(String message, MessageType type, String logger)
	{
		this(message, type, System.currentTimeMillis(), Level.INFO, Thread.currentThread().getName(), "", null, null);
	}
	
	public String getMessage()
	{
		return mMessage;
	}
	
	public void setMessage(String message)
	{
		mMessage = message;
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
	
	public String getServerName()
	{
		return mServerName;
	}
	
	public String getServerId()
	{
		return mServerId;
	}
	
	public void setServer(String id, String name)
	{
		mServerId = id;
		mServerName = name;
	}
	
	public Message copyAs(String message)
	{
		return new Message(message, mType, mTime, mLevel, mThread, mLogger, mServerId, mServerName);
	}
	
	public boolean isRoughDuplicate(Message message)
	{
		return message.mLevel.equals(mLevel) && message.mMessage.equals(mMessage); 
	}
	
	private static Pattern mPattern = Pattern.compile("%([\\-\\+ 0\\,\\(]*)?(\\d+)?(?:(message|msg|m)|(level|p)|(thread|t)|(?:date|d)\\{(.*?)\\}|(server|srv)|(serverid|sid)|(n))");
	
	public String getFormatted(String format)
	{
		Matcher matcher = mPattern.matcher(format);
		
		StringBuffer buffer = new StringBuffer();
		while(matcher.find())
		{
			Object replacement = null;
			// Get the message part
			if(matcher.group(3) != null) // Message
				replacement = mMessage;
			else if(matcher.group(4) != null) // Level
				replacement = mLevel.getLocalizedName();
			else if(matcher.group(5) != null) // Thread
				replacement = getOrEmpty(mThread);
			else if(matcher.group(6) != null) // Date
				replacement = new SimpleDateFormat(matcher.group(6)).format(mTime);
			else if(matcher.group(7) != null) // ServerName
				replacement = getOrEmpty(mServerName);
			else if(matcher.group(8) != null) // ServerId
				replacement = getOrEmpty(mServerId);
			else if(matcher.group(9) != null) // Newline
				replacement = ""; // Ignoring as newline is already present
			
			// Apply any formatting to the message (padding, alignment, etc)
			StringBuilder finalFormat = new StringBuilder();
			finalFormat.append("%");
			// Flags
			if (matcher.group(1) != null)
				finalFormat.append(matcher.group(1));
			// Width
			if (matcher.group(2) != null)
				finalFormat.append(matcher.group(2));
			
			finalFormat.append('s');
			
			matcher.appendReplacement(buffer, Matcher.quoteReplacement(String.format(finalFormat.toString(), replacement)));
		}
		
		matcher.appendTail(buffer);
		return buffer.toString();
	}
	
	private String getOrEmpty(String str)
	{
		if(str == null)
			return "";
		return str;
	}
	
	@Override
	public String toString()
	{
		return String.format("Message: %d [%s] %s %s", mTime, mServerId, mLevel.toString(), mMessage);
	}
}
