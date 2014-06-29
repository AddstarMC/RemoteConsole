package au.com.addstar.rcon.commands;

import au.com.addstar.rcon.commands.misc.KickCommand;
import au.com.addstar.rcon.commands.misc.WhoCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class RconCommand extends Command implements TabExecutor
{
	private CommandDispatcher mDispatcher;
	public RconCommand()
	{
		super("brcon");
		mDispatcher = new CommandDispatcher("Provides access to all rcon commands");
		
		mDispatcher.registerCommand(new AccountCommand());
		mDispatcher.registerCommand(new WhoCommand());
		mDispatcher.registerCommand(new KickCommand());
	}
	
	@Override
	public String getPermission()
	{
		return "rcon.base";
	}
	
	@Override
	public void execute( CommandSender sender, String[] args )
	{
		mDispatcher.dispatchCommand(sender, "/", "brcon", args);
	}

	@Override
	public Iterable<String> onTabComplete( CommandSender sender, String[] args )
	{
		return mDispatcher.tabComplete(sender, "/", "brcon", args);
	}

	
}
