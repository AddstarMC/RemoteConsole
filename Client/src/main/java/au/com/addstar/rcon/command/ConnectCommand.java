package au.com.addstar.rcon.command;

import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;

public class ConnectCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "connect";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getUsage()
	{
		return "<command> <host> [port]";
	}

	@Override
	public String getDescription()
	{
		return "Connects to the specified host";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length != 1 && args.length != 2)
			return false;
		
		String host = args[0];
		int port = 22050;
		
		if(args.length == 2)
		{
			try
			{
				port = Integer.parseInt(args[1]);
				if(port <= 0 || port > 65535)
				{
					screen.printString("Port number is out of range");
					return true;
				}
			}
			catch(NumberFormatException e)
			{
				screen.printString("Port number is not a number");
				return true;
			}
		}
		
		ClientMain.getConnectionManager().addConnection(host, port);
		try
		{
			ClientMain.getConnectionManager().connectAll();
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		return null;
	}

}
