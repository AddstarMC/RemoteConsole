package au.com.addstar.rcon.command;

import java.util.List;

import au.com.addstar.rcon.ConsoleScreen;

public interface ICommand
{
	String getName();
	
	String[] getAliases();
	
	String getUsage();
	
	String getDescription();
	
	boolean onCommand(ConsoleScreen screen, String label, String[] args);
	
	List<String> tabComplete(ConsoleScreen screen, String label, String[] args);
}
