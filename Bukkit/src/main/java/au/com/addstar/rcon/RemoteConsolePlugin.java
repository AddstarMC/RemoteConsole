package au.com.addstar.rcon;

import au.com.addstar.rcon.commands.RconCommand;
import au.com.addstar.rcon.config.Config;
import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerLoginHandler;
import au.com.addstar.rcon.server.auth.IUserStore;
import au.com.addstar.rcon.server.auth.MySQLUserStore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Random;

public class RemoteConsolePlugin extends JavaPlugin
{
	private RconServer mServer;
	private RemoteConsoleAppender mAppender;
	private Config mConfig;
	
	public static RemoteConsolePlugin instance;
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		if(!getDataFolder().exists())
			if(!getDataFolder().mkdirs()){
        System.err.println("[RCON] Unable to create data folders. Error loading config.");
        return;
      };
		
		mConfig = new Config(new File(getDataFolder(), "config.yml"));
		if(!mConfig.load())
		{
			System.err.println("[RCON] Unable to start RconServer. Error loading config.");
			return;
		}
		mConfig.save();
		
		HandlerCreator creator = new HandlerCreator()
		{
			@Override
			public INetworkHandler newHandlerLogin( NetworkManager manager )
			{
				manager.setDebug(mConfig.debug);
				return new ServerLoginHandler(manager);
			}
			
			@Override
			public INetworkHandler newHandlerMain( NetworkManager manager )
			{
				manager.setDebug(mConfig.debug);
				return new NetHandler(manager);
			}
		};
		
		IUserStore userstore;

    if(mConfig.store.equalsIgnoreCase("mysql")) {
			Properties props = new Properties();
			props.put("user",mConfig.databaseUsername);
			props.put("password",mConfig.databasePassword);
			props.put("useSSL",mConfig.databaseUseSSL);
			userstore = new MySQLUserStore(mConfig.databaseHost, mConfig.databaseName,props);
		}
		else
			userstore = new YamlUserStore(new File(getDataFolder(), "users.yml"));
		
		String serverName = mConfig.serverName;
		if(serverName.isEmpty()) {
			getLogger().warning("No Server NAME Configured - you must have a server name since " +
					"1.14 - a random name has been assigned");
			serverName = Bukkit.getServer().getName()+new Random().nextInt();
			mConfig.serverName = serverName;
		}

		
		loadLogAppender();
		
		mServer = new BukkitRconServer(mConfig.port, serverName, userstore);
		
		loadWhitelist();
		
		try
		{
			getLogger().info("Starting RconServer on port " + mConfig.port);
			mServer.start(creator);
		}
		catch(IOException e)
		{
			System.err.println("[RCON] Unable to start RconServer:");
			if(mConfig.debug)e.printStackTrace();
			mServer = null;
			
			return;
		}
		
		new RconCommand().registerAs(getCommand("rcon"));
		
		Bukkit.getScheduler().runTask(this, () -> mServer.openServer());
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
		
		try
		{
			if(mServer != null)
				mServer.shutdown();
		}
		catch(IOException e)
		{
			System.err.println("[RCON] An error occured while shutting down RconServer:");
			if(mConfig.debug)e.printStackTrace();
		}
	}
	
	public String getConsoleFormat()
	{
		return mAppender.getFormat();
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
	
	public boolean loadWhitelist()
	{
		File whitelist = new File(getDataFolder(), "whitelist.txt");
		if (whitelist.exists())
		{
			try
			{
				mServer.getWhitelist().load(whitelist);
			}
			catch (IOException e)
			{
				getLogger().severe("Failed to load whitelist:");
				if(mConfig.debug)e.printStackTrace();
				return false;
			}
		}
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
