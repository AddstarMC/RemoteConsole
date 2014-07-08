package au.com.addstar.rcon;

import java.util.logging.LogRecord;

import net.md_5.bungee.api.CommandSender;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.server.User;
import au.com.addstar.rcon.util.Message;

public class BungeeUser extends User
{
	private UserCommandSender mSender;
	
	public BungeeUser(String name)
	{
		super(name);
	}
	
	@Override
	public void setManager( NetworkManager manager )
	{
		super.setManager(manager);
		
		if(manager == null)
			mSender = null;
		else
			mSender = new UserCommandSender(this);
	}
	
	public CommandSender asCommandSender()
	{
		return mSender;
	}
	
	public void sendLog( LogRecord record )
	{
		MessageType type = MessageType.Log;
		if(record.getThrown() != null)
			type = MessageType.Exception;
		
		int threadId = record.getThreadID();
		String threadName = "";
		for(Thread thread : Thread.getAllStackTraces().keySet())
		{
			if(thread.getId() == threadId)
			{
				threadName = thread.getName();
				break;
			}
		}
		
		Message message = new Message(record.getMessage(), type, record.getMillis(), record.getLevel(), threadName, record.getLoggerName());
		getManager().sendPacket(new PacketOutMessage(message));
	}
}
