package au.com.addstar.rcon;

import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkHandler;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.server.RconServer;

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
			public NetworkHandler newHandler( NetworkManager manager )
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
