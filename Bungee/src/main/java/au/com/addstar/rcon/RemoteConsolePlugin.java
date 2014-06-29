package au.com.addstar.rcon;

import java.io.File;
import java.io.IOException;

import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerLoginHandler;
import net.md_5.bungee.api.plugin.Plugin;

public class RemoteConsolePlugin extends Plugin
{
	public static RemoteConsolePlugin instance;
	
	private RconServer mServer;
	
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
	}
	
	@Override
	public void onDisable()
	{
		mServer.shutdown();
	}
}
