package au.com.addstar.rcon.commands;

import java.util.EnumSet;
import java.util.List;

import net.md_5.bungee.api.CommandSender;

import au.com.addstar.rcon.commands.account.AddCommand;
import au.com.addstar.rcon.commands.account.PasswordCommand;
import au.com.addstar.rcon.commands.account.RemoveCommand;

public class AccountCommand extends CommandDispatcher implements ICommand
{
	public AccountCommand()
	{
		super("Allows you to manage rcon accounts");
		
		registerCommand(new AddCommand());
		registerCommand(new PasswordCommand());
		registerCommand(new RemoveCommand());
	}
	
	@Override
	public String getName()
	{
		return "account";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.account.manage";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <options>";
	}

	@Override
	public String getDescription()
	{
		return "Allows you to manage rcon accounts";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args )
	{
		return super.dispatchCommand(sender, parent, label, args);
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return super.tabComplete(sender, parent, label, args);
	}

}
