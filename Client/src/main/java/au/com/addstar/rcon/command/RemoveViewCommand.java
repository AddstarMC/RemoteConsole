package au.com.addstar.rcon.command;

import java.util.ArrayList;
import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;

public class RemoveViewCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "removeview";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getUsage()
	{
		return "<command> <view>";
	}

	@Override
	public String getDescription()
	{
		return "Removes a view";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length != 1)
			return false;
		
		if(args[0].startsWith("*"))
			throw new IllegalArgumentException("Cannot remove an auto view");
		
		ClientMain.getViewManager().removeView(args[0]);
		
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
