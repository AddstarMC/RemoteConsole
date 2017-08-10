package au.com.addstar.rcon.command;

import java.util.ArrayList;
import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;
import au.com.addstar.rcon.view.ConsoleView;

public class ViewCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "view";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"switchview"};
	}

	@Override
	public String getUsage()
	{
		return "<command> [view]";
	}

	@Override
	public String getDescription()
	{
		return "Changes which view is active.";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length == 0)
		{
			screen.printString("Current view is " + ClientMain.getViewManager().getActive().getName());
			return true;
		}
		
		if(args.length != 1)
			return false;
		
		ConsoleView view = ClientMain.getViewManager().getView(args[0]);
		if(view == null)
			throw new IllegalArgumentException("Unknown view " + args[0]);
		
		ClientMain.getViewManager().setActive(args[0]);
		screen.printString("Set current view to " + args[0]);
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length == 1)
		{
			ArrayList<String> matches = new ArrayList<>();
			for(String name : ClientMain.getViewManager().getViewNames())
			{
				if(name.toLowerCase().startsWith(args[0].toLowerCase()))
					matches.add(name);
			}
			
			return matches;
		}
		return null;
	}

}
