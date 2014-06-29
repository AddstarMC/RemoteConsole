package au.com.addstar.rcon;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import au.com.addstar.rcon.commands.RconCommand;
import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerLoginHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class RemoteConsolePlugin extends Plugin
{
	public static RemoteConsolePlugin instance;
	
	private RconServer mServer;
	private RemoteConsoleLogHandler mLogHandler;
	
	private Formatter mFormatter;
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		if(!getDataFolder().exists())
			getDataFolder().mkdirs();
		
		mServer = new BungeeRconServer(22050, new File(getDataFolder(), "users.yml"));
		
		try
		{
			mServer.load();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return;
		}
		
		mServer.start(new HandlerCreator()
		{
			@Override
			public INetworkHandler newHandlerLogin( NetworkManager manager )
			{
				return new ServerLoginHandler(manager);
			}
			
			@Override
			public INetworkHandler newHandlerMain( NetworkManager manager )
			{
				return new NetHandler(manager);
			}
		});
		
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new RconCommand());
		installLogHandler();
	}
	
	@Override
	public void onDisable()
	{
		mServer.shutdown();
		
		Logger bungeeCordLog = ProxyServer.getInstance().getLogger();
		bungeeCordLog.removeHandler(mLogHandler);
	}
	
	private void installLogHandler()
	{
		Logger bungeeCordLog = ProxyServer.getInstance().getLogger();
		
		mFormatter = null;
		for(Handler handler : bungeeCordLog.getHandlers())
		{
			if(handler instanceof FileHandler && handler.getFormatter() != null)
				mFormatter = handler.getFormatter();
			
			if(mFormatter != null)
				break;
		}
		
		mLogHandler = new RemoteConsoleLogHandler();
		mLogHandler.setFormatter(mFormatter);
		bungeeCordLog.addHandler(mLogHandler);
	}
	
	public static String formatMessage(String message)
	{
		message = instance.mFormatter.format(new LogRecord(Level.INFO, message));
		if(message.endsWith("\n"))
			message = message.substring(0, message.length()-1);
		
		return message;
	}
}
