package au.com.addstar.rcon.command;

import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;
import au.com.addstar.rcon.network.ClientConnection;

public class WhoCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "who";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getUsage()
	{
		return "<command>";
	}

	@Override
	public String getDescription()
	{
		return "Displays what server you are connected to";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length != 0)
			return false;
		
		ClientConnection connection = ClientMain.getConnectionManager().getActive();
		
		if(connection == null)
			screen.printString("You are not connected to a server");
		else
			screen.printString("You are connected to " + connection.getId());
		
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		return null;
	}

}
