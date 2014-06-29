package au.com.addstar.rcon;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.AbstractNetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerServer;
import au.com.addstar.rcon.network.packets.main.PacketInCommand;
import au.com.addstar.rcon.network.packets.main.PacketInTabComplete;
import au.com.addstar.rcon.network.packets.main.PacketOutTabComplete;
import au.com.addstar.rcon.server.ServerNetworkManager;

public class NetHandler extends AbstractNetworkHandler implements INetworkMainHandlerServer
{
	public NetHandler(NetworkManager manager)
	{
		super(manager);
	}
	
	@Override
	public void handleCommand( final PacketInCommand packet )
	{
		ProxyServer.getInstance().getScheduler().schedule(RemoteConsolePlugin.instance, new Runnable()
		{
			@SuppressWarnings( "deprecation" )
			@Override
			public void run()
			{
				BungeeUser user = (BungeeUser)((ServerNetworkManager)getManager()).getUser();

				if(!ProxyServer.getInstance().getPluginManager().dispatchCommand(user.asCommandSender(), packet.command))
					user.asCommandSender().sendMessage(ChatColor.RED + "Command not found");
			}
		}, 0, TimeUnit.MILLISECONDS);
	}

	@Override
	public void handleTabComplete( final PacketInTabComplete packet )
	{
		ProxyServer.getInstance().getScheduler().schedule(RemoteConsolePlugin.instance, new Runnable()
		{
			@Override
			public void run()
			{
				BungeeUser user = (BungeeUser)((ServerNetworkManager)getManager()).getUser();
				ArrayList<String> results = new ArrayList<String>();

				ProxyServer.getInstance().getPluginManager().dispatchCommand(user.asCommandSender(), packet.message, results);
				getManager().sendPacket(new PacketOutTabComplete(results));
					
			}
		}, 0, TimeUnit.MILLISECONDS);
	}
}
