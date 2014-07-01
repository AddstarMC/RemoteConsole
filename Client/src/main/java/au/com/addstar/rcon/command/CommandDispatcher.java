package au.com.addstar.rcon.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import au.com.addstar.rcon.ConsoleScreen;

public class CommandDispatcher
{
	private HashMap<String, ICommand> mCommands;
	
	public CommandDispatcher()
	{
		mCommands = new HashMap<String, ICommand>();
	}
	
	public void registerCommand(ICommand command)
	{
		mCommands.put(command.getName().toLowerCase(), command);
		
		String[] aliases = command.getAliases();
		if(aliases != null)
		{
			for(String alias : aliases)
				mCommands.put(alias.toLowerCase(), command);
		}
	}

	public void dispatchCommand(ConsoleScreen screen, String message)
	{
		String[] parts = message.split(" ");
		if(parts[0].startsWith("."))
			parts[0] = parts[0].substring(1);
		ICommand command = mCommands.get(parts[0].toLowerCase());
		
		if(command == null)
		{
			screen.printString("Unknown command " + parts[0]);
			return;
		}
		
		try
		{
			if(!command.onCommand(screen, parts[0], Arrays.copyOfRange(parts, 1, parts.length)))
				screen.printString("Usage: ." + command.getUsage().replaceAll("<command>", parts[0]));
		}
		catch(IllegalArgumentException e)
		{
			if(e.getMessage() == null)
				throw e;
			
			screen.printString(e.getMessage());
		}
	}
	
	public List<String> tabComplete(ConsoleScreen screen, String message)
	{
		String[] parts = message.split(" ");
		if(parts[0].startsWith("."))
			parts[0] = parts[0].substring(1);
		
		if(parts.length == 1)
		{
			ArrayList<String> commands = new ArrayList<String>();
			for(String name : mCommands.keySet())
			{
				if(name.startsWith(parts[0].toLowerCase()))
					commands.add("." + name);
			}
			
			return commands;
		}
		
		ICommand command = mCommands.get(parts[0].toLowerCase());
		
		if(command == null)
			return Collections.emptyList();
		
		return command.tabComplete(screen, parts[0], Arrays.copyOfRange(parts, 1, parts.length));
	}
}
