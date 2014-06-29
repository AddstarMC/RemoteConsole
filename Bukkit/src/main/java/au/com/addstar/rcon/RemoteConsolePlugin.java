package au.com.addstar.rcon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.rcon.commands.RconCommand;
import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerLoginHandler;

public class RemoteConsolePlugin extends JavaPlugin
{
	private RconServer mServer;
	private RemoteConsoleAppender mAppender;
	
	public static RemoteConsolePlugin instance;
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		if(!getDataFolder().exists())
			getDataFolder().mkdirs();
		
		mServer = new BukkitRconServer(22050, new File(getDataFolder(), "users.yml"), this);
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
		
		new RconCommand().registerAs(getCommand("rcon"));
		
		loadLogAppender();
	}
	
	@Override
	public void onDisable()
	{
		if(mAppender != null)
		{
			Logger log = (Logger)LogManager.getRootLogger();
			log.removeAppender(mAppender);
			mAppender = null;
		}
		
		mServer.shutdown();
	}
	
	private boolean loadLogAppender()
	{
		Logger log = (Logger)LogManager.getRootLogger();
		for(Appender appender : log.getAppenders().values())
		{
			if(appender instanceof RemoteConsoleAppender)
				log.removeAppender(appender);
		}
		
		mAppender = new RemoteConsoleAppender(new DefaultConfiguration());
		mAppender.start();
		log.addAppender(mAppender);
		
		return true;
	}
	
	private static CommandMap mCommandMap = null;
	
	public static CommandMap getCommandMap()
	{
		if(mCommandMap != null)
			return mCommandMap;
		
		try
		{
			Method method = Bukkit.getServer().getClass().getMethod("getCommandMap");
			mCommandMap = (CommandMap)method.invoke(Bukkit.getServer());
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		return mCommandMap;
	}
}
