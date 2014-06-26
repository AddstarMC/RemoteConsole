package au.com.addstar.rcon.commands;

public class RconCommand extends RootCommandDispatcher
{
	public RconCommand()
	{
		super("Views and managers rcon connections and accounts");
		
		registerCommand(new AccountCommand());
	}
}
