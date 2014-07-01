package au.com.addstar.rcon.command;

import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;

public class ServersCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "servers";
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
		return "Gets a list of all connected servers";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length != 0)
			return false;
		
		StringBuilder builder = new StringBuilder();
		
		for(String name : ClientMain.getConnectionManager().getConnectionNames())
		{
			if(builder.length() != 0)
				builder.append(", ");
			builder.append(name);
		}
		
		screen.printString("Connected Servers:");
		screen.printString(builder.toString());
		
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		return null;
	}

}
