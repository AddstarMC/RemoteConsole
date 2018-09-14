package au.com.addstar.rcon;

import au.com.addstar.rcon.commands.RconCommand;
import au.com.addstar.rcon.config.MainConfig;
import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerLoginHandler;
import au.com.addstar.rcon.server.auth.IUserStore;
import au.com.addstar.rcon.server.auth.MySQLUserStore;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class RemoteConsolePlugin extends Plugin
{
	public static RemoteConsolePlugin instance;
	
	private RconServer mServer;
	private RemoteConsoleLogHandler mLogHandler;
	private MainConfig mConfig;
	
	private Formatter mFormatter;
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		if(!getDataFolder().exists())
			getDataFolder().mkdirs();
		
		mConfig = new MainConfig();
		
		try
		{
			mConfig.init(new File(getDataFolder(), "config.yml"));
			mConfig.checkValid();
		}
		catch ( InvalidConfigurationException e )
		{
			System.err.println("[RCON] Unable to start RconServer. Error loading config:");
			e.printStackTrace();
			return;
		}
		
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
		
		IUserStore userstore = null;
		
		if(mConfig.store.equalsIgnoreCase("mysql"))
			userstore = new MySQLUserStore(mConfig.databaseHost, mConfig.databaseName, mConfig.databaseUsername, mConfig.databaseUsername);
		else
			userstore = new YamlUserStore(new File(getDataFolder(), "users.yml"));
		
		String serverName = mConfig.serverName;
		if(serverName == null)
			serverName = "Proxy";
		
		installLogHandler();
		
		mServer = new BungeeRconServer(mConfig.port, serverName, userstore);
		
		loadWhitelist();
		
		try
		{
			getLogger().info("Starting RconServer on port " + mConfig.port);
			mServer.start(creator);
			mServer.openServer();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			mServer = null;
			return;
		}
		
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new RconCommand());
	}
	
	@Override
	public void onDisable()
	{
		Logger bungeeCordLog = ProxyServer.getInstance().getLogger();
		bungeeCordLog.removeHandler(mLogHandler);
		
		try
		{
			if(mServer != null)
				mServer.shutdown();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return;
		}
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
		if(mFormatter != null){mLogHandler.setFormatter(mFormatter);}
		else{
			System.out.println("[RemoteConsole]Log formatter was null thus the console handler " +
				"has no formatter set");
			mFormatter = new SimpleFormatter();
			mLogHandler.setFormatter(mFormatter);
		}
		bungeeCordLog.addHandler(mLogHandler);
	}
	
	public static String formatMessage(String message)
	{
		message = instance.mFormatter.format(new LogRecord(Level.INFO, message));
		if(message.endsWith("\n"))
			message = message.substring(0, message.length()-1);
		
		return message;
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
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}
