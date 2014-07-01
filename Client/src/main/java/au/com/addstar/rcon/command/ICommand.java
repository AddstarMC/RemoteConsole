package au.com.addstar.rcon.command;

import java.util.List;

import au.com.addstar.rcon.ConsoleScreen;

public interface ICommand
{
	public String getName();
	
	public String[] getAliases();
	
	public String getUsage();
	
	public String getDescription();
	
	public boolean onCommand(ConsoleScreen screen, String label, String[] args);
	
	public List<String> tabComplete(ConsoleScreen screen, String label, String[] args);
}
