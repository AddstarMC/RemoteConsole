package au.com.addstar.rcon.command;

import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;

public class ViewsCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "views";
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
		return "Lists all views";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length != 0)
			return false;
		
		StringBuilder builder = new StringBuilder();
		for(String view : ClientMain.getViewManager().getViewNames())
		{
			if(builder.length() != 0)
				builder.append(", ");
			builder.append(view);
		}
		
		screen.printString("Available views:");
		screen.printString(builder.toString());
		
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		return null;
	}

}
