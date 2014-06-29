package au.com.addstar.rcon.commands.account;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import au.com.addstar.rcon.BungeeRconServer;
import au.com.addstar.rcon.BungeeUser;
import au.com.addstar.rcon.commands.BadArgumentException;
import au.com.addstar.rcon.commands.CommandSenderType;
import au.com.addstar.rcon.commands.ICommand;
import au.com.addstar.rcon.server.RconServer;

public class RemoveCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "remove";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.account.manage.remove";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <username>";
	}

	@Override
	public String getDescription()
	{
		return "Removes a user account";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Console);
	}

	@SuppressWarnings( "deprecation" )
	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length != 1)
			return false;
		
		BungeeRconServer server = (BungeeRconServer)RconServer.instance;
		BungeeUser user = (BungeeUser)server.getUser(args[0]);
		
		if(user == null)
			throw new BadArgumentException(0, "Unknown account");
		
		if(user.getManager() != null)
			user.getManager().getNetHandler().disconnect("Your account has been removed");
		
		try
		{
			server.removeUser(user);
			server.save();
			
			sender.sendMessage("That user has been removed.");
		}
		catch(IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
		catch ( IOException e )
		{
			sender.sendMessage(ChatColor.RED + "An error occured while saving account data.");
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
