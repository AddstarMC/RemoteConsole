package au.com.addstar.rcon;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerNetworkManager;

public class RemoteConsoleLogHandler extends Handler
{
	@Override
	public void publish( LogRecord record )
	{
		MessageType type = MessageType.Log;
		if(record.getThrown() != null)
			type = MessageType.Exception;
		
		String message = getFormatter().format(record);
		if(message.endsWith("\n"))
			message = message.substring(0, message.length()-1);
		
		for(ServerNetworkManager connection : RconServer.instance.getConnections())
		{
			BungeeUser user = (BungeeUser)connection.getUser();
			if(user != null)
				((UserCommandSender)user.asCommandSender()).sendMessage(message, type);
		}
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void close() throws SecurityException
	{
	}

}
