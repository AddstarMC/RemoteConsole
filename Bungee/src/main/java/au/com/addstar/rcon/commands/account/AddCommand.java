package au.com.addstar.rcon.commands.account;

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
import au.com.addstar.rcon.server.auth.StoredPassword;

public class AddCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "add";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.account.manage.add";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <accountName> <password> [restricted]";
	}

	@Override
	public String getDescription()
	{
		return "Adds an account with the specified name. If 'restricted' is specified, the account will only be able to run commands that are in the whitelist";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@SuppressWarnings( "deprecation" )
	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length != 2 && args.length != 3)
			return false;
		
		BungeeRconServer server = (BungeeRconServer)RconServer.instance;
		
		BungeeUser user = (BungeeUser)server.getUser(args[0]);
		
		if(user != null)
			throw new BadArgumentException(0, "An account by that name already exists");
		
		String password = args[1];
		
		boolean restricted = false;
		if (args.length == 3)
		{
			if (args[2].equalsIgnoreCase("restricted"))
				restricted = true;
			else
				throw new BadArgumentException(2, "Expected 'restricted' or nothing");
		}
		
		if(server.createUser(args[0], StoredPassword.generate(password), restricted))
			sender.sendMessage(ChatColor.GREEN + "Account " + args[0] + " was successfully created.");
		else
			sender.sendMessage(ChatColor.RED + "An error occured while saving the new account.");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
