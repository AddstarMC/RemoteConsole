package au.com.addstar.rcon;

public class Event
{
	public enum EventType
	{
		Quit,
		ConnectionShutdown
	}
	
	private EventType mType;
	private Object mArgument;
	
	public Event(EventType type)
	{
		this(type, null);
	}
	
	public Event(EventType type, Object argument)
	{
		mType = type;
		mArgument = argument;
	}
	
	public EventType getType()
	{
		return mType;
	}
	
	@SuppressWarnings( "unchecked" )
	public <T> T getArgument()
	{
		return (T)mArgument;
	}
}
