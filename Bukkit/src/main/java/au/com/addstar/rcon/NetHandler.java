
package au.com.addstar.rcon;

import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.server.ServerCommandEvent;

import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.AbstractNetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerServer;
import au.com.addstar.rcon.network.packets.main.PacketInCommand;
import au.com.addstar.rcon.network.packets.main.PacketInPassword;
import au.com.addstar.rcon.network.packets.main.PacketInTabComplete;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.network.packets.main.PacketOutTabComplete;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerNetworkManager;
import au.com.addstar.rcon.server.auth.StoredPassword;
import au.com.addstar.rcon.util.Message;

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
				
				if (user.isRestricted())
				{
					if (!RconServer.instance.getWhitelist().isWhitelisted(packet.command))
					{
						user.asCommandSender().sendMessage(ChatColor.RED + "You are not permitted to use that command");
						return;
					}
				}
				
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
				
				if (user.isRestricted())
				{
					if (!RconServer.instance.getWhitelist().isWhitelisted(packet.message))
					{
						getManager().sendPacket(new PacketOutTabComplete(Collections.<String>emptyList()));
						return;
					}
				}
				
				getManager().sendPacket(new PacketOutTabComplete(RemoteConsolePlugin.getCommandMap().tabComplete(user.asCommandSender(), packet.message)));
			}
		});
	}
	
	@Override
	public void handlePassword( final PacketInPassword packet )
	{
		Bukkit.getScheduler().runTask(RemoteConsolePlugin.instance, new Runnable()
		{
			@Override
			public void run()
			{
				BukkitUser user = (BukkitUser)((ServerNetworkManager)getManager()).getUser();
				StoredPassword current = user.getPassword();
				
				if (current.matches(packet.previousPassword))
				{
					user.setPassword(StoredPassword.generate(packet.newPassword));
					if(!RconServer.instance.saveUser(user))
						getManager().sendPacket(new PacketOutMessage(new Message(ChatColor.RED + "Unable to save changes, an internal error occured.", MessageType.Directed, "RemoteConsole")));
					else
						getManager().sendPacket(new PacketOutMessage(new Message(ChatColor.YELLOW + "Your password has been updated.", MessageType.Directed, "RemoteConsole")));
				}
				else
					getManager().sendPacket(new PacketOutMessage(new Message(ChatColor.RED + "Your password did not match your current password", MessageType.Directed, "RemoteConsole")));
			}
		});
	}
}
