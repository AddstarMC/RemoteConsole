package au.com.addstar.rcon.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public enum CommandSenderType
{
	/**
	 * Can be Player
	 */
	Player,
	/**
	 * Can be ConsoleCommandSender OR RemoteConsoleCommandSender
	 */
	Console;
	
	public boolean matches(CommandSender sender)
	{
		switch(this)
		{
		case Player:
			return (sender instanceof ProxiedPlayer);
		case Console:
			return !(sender instanceof ProxiedPlayer);
		default:
			return false;
		}
	}
	
	public static CommandSenderType from(CommandSender sender)
	{
		if(sender instanceof ProxiedPlayer)
			return Player;
		return Console;
	}
	
	@Override
	public String toString()
	{
		switch(this)
		{
		case Console:
			return "Console";
		case Player:
			return "Player";
		default:
			return name();
		}
	}
}
