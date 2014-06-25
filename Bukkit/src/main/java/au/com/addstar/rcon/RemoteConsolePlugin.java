package au.com.addstar.rcon;

import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerLoginHandler;

public class RemoteConsolePlugin extends JavaPlugin
{
	private RconServer mServer;
	
	@Override
	public void onEnable()
	{
		mServer = new RconServer(22050);
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
