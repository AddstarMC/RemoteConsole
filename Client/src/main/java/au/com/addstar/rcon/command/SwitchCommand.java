package au.com.addstar.rcon.command;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;

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
		ClientMain.getConnectionManager().getActive().getMessageBuffer().display(screen, EnumSet.allOf(MessageType.class));
		screen.printString("Switched active server to " + server);
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length == 1)
		{
			ArrayList<String> matches = new ArrayList<String>();
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
