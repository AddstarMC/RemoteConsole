package au.com.addstar.rcon;

import java.io.Serializable;

import io.netty.util.CharsetUtil;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerNetworkManager;

public class RemoteConsoleAppender extends AbstractAppender
{
	private static Layout<? extends Serializable> mLayout;
	public RemoteConsoleAppender(Configuration config)
	{
		super("RemoteConsoleAppender", null, null);
		
		Logger root = (Logger)LogManager.getRootLogger();
		Appender appender = root.getAppenders().get("TerminalConsole");
		if(appender != null)
			mLayout = appender.getLayout();
		else
			mLayout = PatternLayout.createLayout("[%d{HH:mm:ss} %level]: %msg", config, null, "UTF-8", "true");
	}

	@Override
	public void append( LogEvent event )
	{
		MessageType type = MessageType.Log;
		if(event.getThrown() != null)
			type = MessageType.Exception;
		if(event.getThreadName().equals("Async Chat Thread"))
			type = MessageType.Chat;
		
		String message = new String(mLayout.toByteArray(event), CharsetUtil.UTF_8);
		if(message.endsWith("\n"))
			message = message.substring(0, message.length()-1);
		
		for(ServerNetworkManager connection : RconServer.instance.getConnections())
		{
			BukkitUser user = (BukkitUser)connection.getUser();
			user.sendMessage(message, type);
		}
	}
	
	public static String formatMessage(String message)
	{
		LogEvent event = new Log4jLogEvent("Minecraft", null, RemoteConsoleAppender.class.getName(), Level.INFO, new SimpleMessage(message), null);
		message = new String(mLayout.toByteArray(event), CharsetUtil.UTF_8);
		
		if(message.endsWith("\n"))
			message = message.substring(0, message.length()-1);
		
		return message;
	}

}
