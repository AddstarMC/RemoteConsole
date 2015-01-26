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
		return "<command> {<host> [port]|<alias>}";
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
		
		String name = null;
		String host;
		// Try alias first
		host = ClientMain.getConnectionManager().resolveAlias(args[0]);
		if (host == null)
			host = args[0];
		else
			name = args[0];
		
		int port = 22050;
		boolean portDone = false;
		
		if (host.contains(":"))
		{
			String[] parts = host.split(":", 2);
			try
			{
				host = parts[0];
				port = Integer.parseInt(parts[1]);
				if(port <= 0 || port > 65535)
				{
					screen.printString("Port number is out of range");
					return true;
				}
			}
			catch (NumberFormatException e)
			{
				screen.printString(parts[1] + " is not a port number");
				return true;
			}
			portDone = true;
		}
		
		if(args.length == 2)
		{
			if (portDone)
				return false;
			
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
				screen.printString(args[1] + " is not a port number");
				return true;
			}
		}
		
		ClientMain.getConnectionManager().addConnection(host, port, name);
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
