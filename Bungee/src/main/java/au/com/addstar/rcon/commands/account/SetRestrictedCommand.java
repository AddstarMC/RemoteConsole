package au.com.addstar.rcon.commands.account;

import java.util.EnumSet;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import au.com.addstar.rcon.BungeeUser;
import au.com.addstar.rcon.commands.BadArgumentException;
import au.com.addstar.rcon.commands.CommandSenderType;
import au.com.addstar.rcon.commands.ICommand;
import au.com.addstar.rcon.server.RconServer;

public class SetRestrictedCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "setrestricted";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.setrestricted";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <account> <true|false>";
	}

	@Override
	public String getDescription()
	{
		return "Sets an account to be restricted or not";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.allOf(CommandSenderType.class);
	}

	@SuppressWarnings( "deprecation" )
	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length != 2)
			return false;
		
		BungeeUser user = (BungeeUser)RconServer.instance.getUser(args[0]);
		
		if(user == null)
			throw new BadArgumentException(0, "Unknown account");
		
		user.setIsRestricted(Boolean.parseBoolean(args[1]));
		
		if (RconServer.instance.saveUser(user))
		{
			if (user.isRestricted())
				sender.sendMessage(ChatColor.GOLD + user.getName() + " is now a restricted account");
			else
				sender.sendMessage(ChatColor.GOLD + user.getName() + " is now an unrestricted account");
		}
		else
			sender.sendMessage(ChatColor.RED + "An error occurred while saving the user account");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}
}
