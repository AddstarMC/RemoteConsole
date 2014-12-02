package au.com.addstar.rcon;

import java.util.logging.LogRecord;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
		
		String text = record.getMessage();
		if(text == null)
			text = "";
		
		if(record.getParameters() != null)
		{
			for(int i = 0; i < record.getParameters().length; ++i)
				text = text.replace("{" + i + "}", String.valueOf(record.getParameters()[i]));
		}
		
		final Message message = new Message(text, type, record.getMillis(), record.getLevel(), threadName, record.getLoggerName());
		ProxyServer.getInstance().getScheduler().runAsync(RemoteConsolePlugin.instance, new Runnable()
		{
			@Override
			public void run()
			{
				getManager().sendPacket(new PacketOutMessage(message));
			}
		});
	}
}
