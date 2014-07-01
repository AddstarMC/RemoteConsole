package au.com.addstar.rcon.command;

import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.view.CombinedConsoleView;

public class CreateViewCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "createview";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getUsage()
	{
		return "<command> <name> <server> [<server>...]";
	}

	@Override
	public String getDescription()
	{
		return "Creates a custom view of one or more servers";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length < 2)
			return false;
		
		if(args[0].startsWith("*"))
			throw new IllegalArgumentException("Names that start with * are reserved");
		
		if(ClientMain.getViewManager().getView(args[0]) != null)
			throw new IllegalArgumentException("A view with that name already exists");
		
		CombinedConsoleView view = new CombinedConsoleView();
		
		for(int i = 1; i < args.length; ++i)
		{
			ClientConnection con = ClientMain.getConnectionManager().getConnection(args[i]);
			if(con == null)
				throw new IllegalArgumentException("Unknown server " + args[i]);
			
			view.addConnection(con);
		}
		
		ClientMain.getViewManager().addView(args[0], view);
		ClientMain.getViewManager().setActive(args[0]);
		screen.printString("Created and switched to new view");
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		return null;
	}

}
