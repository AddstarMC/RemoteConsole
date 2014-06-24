package au.com.addstar.rcon;

import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.rcon.server.RconServer;

public class RemoteConsolePlugin extends JavaPlugin
{
	private RconServer mServer;
	
	@Override
	public void onEnable()
	{
		mServer = new RconServer(22050);
		mServer.start();
	}
	
	@Override
	public void onDisable()
	{
		mServer.shutdown();
	}
}
