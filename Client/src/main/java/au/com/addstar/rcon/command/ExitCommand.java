package au.com.addstar.rcon.command;

import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;
import au.com.addstar.rcon.Event.EventType;
import au.com.addstar.rcon.Event;

public class ExitCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "exit";
	}
	
	@Override
	public String[] getAliases()
	{
		return null;
	}
	
	@Override
	public String getDescription()
	{
		return "Exits the remote console";
	}
	
	@Override
	public String getUsage()
	{
		return "<command>";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length != 0)
			return false;
		
		ClientMain.callEvent(new Event(EventType.Quit));
		
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		return null;
	}

}
