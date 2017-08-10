package au.com.addstar.rcon.command;

import java.util.ArrayList;
import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;
import au.com.addstar.rcon.network.ClientConnection;

public class DisconnectCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "disconnect";
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
		return "Disconnects from a server";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length != 1)
			return false;
		
		ClientConnection connection = ClientMain.getConnectionManager().getConnection(args[0]);
		if(connection == null)
			throw new IllegalArgumentException("Unknown server " + args[0]);
		
		connection.setShouldReconnect(false);
		connection.getManager().close("Disconnecting");
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
