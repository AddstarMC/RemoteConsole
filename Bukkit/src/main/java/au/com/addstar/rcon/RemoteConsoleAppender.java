package au.com.addstar.rcon;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerNetworkManager;

public class RemoteConsoleAppender extends AbstractAppender
{
	public static PatternLayout layout;
	public RemoteConsoleAppender(Configuration config)
	{
		super("RemoteConsoleAppender", null, null);
		layout = PatternLayout.createLayout("[%d{HH:mm:ss} %level]: %msg", config, null, "UTF-8", "true");
	}

	@Override
	public void append( LogEvent event )
	{
		MessageType type = MessageType.Log;
		if(event.getThrown() != null)
			type = MessageType.Exception;
		if(event.getThreadName().equals("Async Chat Thread"))
			type = MessageType.Chat;
		
		String message = event.getMessage().getFormattedMessage();
		
		for(ServerNetworkManager connection : RconServer.instance.getConnections())
		{
			BukkitUser user = (BukkitUser)connection.getUser();
			user.sendMessage(message, type);
		}
	}

}
