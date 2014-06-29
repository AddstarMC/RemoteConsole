package au.com.addstar.rcon.commands.account;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BukkitRconServer;
import au.com.addstar.rcon.BukkitUser;
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
		return label + " <accountName> <password>";
	}

	@Override
	public String getDescription()
	{
		return "Adds an account with the specified name";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length != 2 && args.length != 3)
			return false;
		
		BukkitRconServer server = (BukkitRconServer)RconServer.instance;
		
		BukkitUser user = (BukkitUser)server.getUser(args[0]);
		
		if(user != null)
			throw new BadArgumentException(0, "An account by that name already exists");
		
		String password = args[1];
		
		if(server.createUser(args[0], StoredPassword.generate(password)))
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
