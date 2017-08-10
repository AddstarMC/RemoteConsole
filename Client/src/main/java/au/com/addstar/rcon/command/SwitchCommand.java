package au.com.addstar.rcon.command;

import java.util.ArrayList;
import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;

public class SwitchCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "switch";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getUsage()
	{
		return "<command> <server>";
	}

	@Override
	public String getDescription()
	{
		return "Switches the active server to <server>";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length != 1)
			return false;
		
		String server = args[0];
		ClientMain.getConnectionManager().switchActive(server);
		screen.printString("Switched active server to " + server);
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length == 1)
		{
			ArrayList<String> matches = new ArrayList<>();
			for(String name : ClientMain.getConnectionManager().getConnectionNames())
			{
				if(name.toLowerCase().startsWith(args[0].toLowerCase()))
					matches.add(name);
			}
			
			return matches;
		}
		return null;
	}

}
