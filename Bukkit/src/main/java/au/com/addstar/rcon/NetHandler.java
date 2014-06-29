
package au.com.addstar.rcon;

import org.bukkit.Bukkit;
import org.bukkit.event.server.ServerCommandEvent;

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
		Bukkit.getScheduler().runTask(RemoteConsolePlugin.instance, new Runnable()
		{
			@Override
			public void run()
			{
				BukkitUser user = (BukkitUser)((ServerNetworkManager)getManager()).getUser();
				ServerCommandEvent event = new ServerCommandEvent(user.asCommandSender(), packet.command);
				Bukkit.getPluginManager().callEvent(event);
				
				String command = event.getCommand();
				if(command == null || command.trim().isEmpty())
					return;
				
				Bukkit.dispatchCommand(user.asCommandSender(), command);
			}
		});
	}
	
	@Override
	public void handleTabComplete( final PacketInTabComplete packet )
	{
		Bukkit.getScheduler().runTask(RemoteConsolePlugin.instance, new Runnable()
		{
			@Override
			public void run()
			{
				BukkitUser user = (BukkitUser)((ServerNetworkManager)getManager()).getUser();
				getManager().sendPacket(new PacketOutTabComplete(RemoteConsolePlugin.getCommandMap().tabComplete(user.asCommandSender(), packet.message)));
			}
		});
	}
}
