package au.com.addstar.rcon.commands;

import au.com.addstar.rcon.commands.misc.KickCommand;
import au.com.addstar.rcon.commands.misc.WhoCommand;

public class RconCommand extends RootCommandDispatcher
{
	public RconCommand()
	{
		super("Views and managers rcon connections and accounts");
		
		registerCommand(new AccountCommand());
		registerCommand(new WhoCommand());
		registerCommand(new KickCommand());
	}
}
