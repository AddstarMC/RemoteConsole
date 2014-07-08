package au.com.addstar.rcon;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerNetworkManager;

public class RemoteConsoleAppender extends AbstractAppender
{
	private String mFormat;
	
	public RemoteConsoleAppender(Configuration config)
	{
		super("RemoteConsoleAppender", null, null);
		
		Logger root = (Logger)LogManager.getRootLogger();
		Appender appender = root.getAppenders().get("TerminalConsole");
		if(appender != null)
		{
			Layout<? extends Serializable> layout = appender.getLayout();
			if(layout instanceof PatternLayout)
				mFormat = ((PatternLayout)layout).getConversionPattern(); 
		}
		
		if(mFormat == null)
			mFormat = "[%d{HH:mm:ss} %level]: %msg";
	}

	@Override
	public void append( LogEvent event )
	{
		for(ServerNetworkManager connection : RconServer.instance.getConnections())
		{
			BukkitUser user = (BukkitUser)connection.getUser();
			if(user != null)
				user.sendLog(event);
		}
	}
	
	public String getFormat()
	{
		return mFormat;
	}
}
