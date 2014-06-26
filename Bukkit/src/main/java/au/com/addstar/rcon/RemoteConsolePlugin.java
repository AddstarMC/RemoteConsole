package au.com.addstar.rcon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerLoginHandler;

public class RemoteConsolePlugin extends JavaPlugin
{
	private RconServer mServer;
	
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
	}
	
	@Override
	public void onDisable()
	{
		mServer.shutdown();
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
