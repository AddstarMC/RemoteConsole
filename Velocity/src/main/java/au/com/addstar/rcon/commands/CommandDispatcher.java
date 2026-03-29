package au.com.addstar.rcon.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.velocitypowered.api.command.CommandSource;

/**
 * This allows sub commands to be handled in a clean easily expandable way.
 * Just create a new command that implements ICommand
 * 
 * @author Schmoller
 *
 */
@SuppressWarnings( "deprecation" )
public class CommandDispatcher
{
	public static Pattern usageArgumentPattern = Pattern.compile("(\\[<.*?>\\])|(\\[.*?\\])|(<.*?>)");
	
	private String mRootCommandDescription;
	private HashMap<String, ICommand> mCommands;
	
	private ICommand mDefaultCommand = null;
	
	public CommandDispatcher(String description)
	{
		mCommands = new HashMap<>();
		
		mRootCommandDescription = description;
		
		registerCommand(new InternalHelp());
	}
	/**
	 * Registers a command to be handled by this dispatcher
	 * @param command
	 */
	public void registerCommand(ICommand command)
	{
		mCommands.put(command.getName().toLowerCase(), command);
	}
	
	public void setDefault(ICommand command)
	{
		mDefaultCommand = command;
	}
	
	public boolean dispatchCommand(CommandSource sender, String parent, String label, String[] args)
	{
		parent += label + " ";
		
		if(args.length == 0 && mDefaultCommand == null)
		{
			displayUsage(sender, parent, label, null);
			return true;
		}
		
		ICommand com = null;
		String subCommand = "";
		
		String[] subArgs = args;
		
		if(args.length > 0)
		{
			subCommand = args[0].toLowerCase();
			subArgs = (args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
			
			if(mCommands.containsKey(subCommand))
				com = mCommands.get(subCommand);
			else
			{
				// Check aliases
	AliasCheck:	for(Entry<String, ICommand> ent : mCommands.entrySet())
				{
					if(ent.getValue().getAliases() != null)
					{
						String[] aliases = ent.getValue().getAliases();
						for(String alias : aliases)
						{
							if(subCommand.equalsIgnoreCase(alias))
							{
								com = ent.getValue();
								break AliasCheck;
							}
						}
					}
				}
			}
		}
		
		if(com == null)
			com = mDefaultCommand;
		
		// Was not found
		if(com == null)
		{
			displayUsage(sender, parent, label, subCommand);
			return true;
		}
		
		// Check that the sender is correct
		if(!com.getAllowedSenders().contains(CommandSourceType.from(sender)))
		{
			if(com == mDefaultCommand)
				displayUsage(sender, parent, label, subCommand);
			else
				sender.sendRichMessage(String.format("<red>%s %s cannot be called from the %s</red>", label, subCommand, CommandSourceType.from(sender)));
			return true;
		}
		
		// Check that they have permission
		if(com.getPermission() != null && !sender.hasPermission(com.getPermission()))
		{
			sender.sendRichMessage(String.format("<red>You do not have permission to use %s %s</red>", label, subCommand));
			return true;
		}
		
		try
		{
			if(!com.onCommand(sender, parent, subCommand, subArgs))
				sender.sendRichMessage("<red>Usage: " + parent + com.getUsageString(subCommand, sender) + "</red>");
		}
		catch(BadArgumentException e)
		{
			String cmdString = "<gray>" + parent;
			for(int i = 0; i < args.length; ++i)
			{
				if(i == e.getArgument() + 1)
					cmdString += "<red>" + args[i] + "<gray>";
				else
					cmdString += args[i];
				
				cmdString += " ";
			}
			
			if(e.getArgument() >= args.length - 1)
				cmdString += "<red>?</red>";
			
			sender.sendRichMessage("<red>Error in command: " + cmdString + "</red>");
			sender.sendRichMessage("<red> " + e.getMessage() + "</red>");
			
			for(String line : e.getInfoLines())
				sender.sendRichMessage("<gray> " + line + "</gray>");
		}
		catch(IllegalArgumentException | IllegalStateException e)
		{
			sender.sendRichMessage("<red>" + e.getMessage() + "</red>");
		}

		return true;
	}
	private void displayUsage(CommandSource sender, String parent, String label, String subcommand)
	{
		String usage = "";
		
		boolean first = true;
		boolean odd = true;
		// Build the list
		for(ICommand command : mCommands.values())
		{
			// Check that the sender is correct
			if(!command.getAllowedSenders().contains(CommandSourceType.from(sender)))
				continue;
			
			// Check that they have permission
			if(command.getPermission() != null && !sender.hasPermission(command.getPermission()))
				continue;
			
			if(odd)
				usage += "<white>";
			else
				usage += "<gray>";
			odd = !odd;
			
			if(first)
				usage += command.getName();
			else
				usage += ", " + command.getName();
			
			first = false;
		}
		
		if(subcommand != null)
			sender.sendRichMessage("<red>Unknown command: <reset>" + parent + "<gold>" + subcommand);
		else
			sender.sendRichMessage("<red>No command specified: <reset>" + parent + "<gold><command>");

		if(!first)
		{
			sender.sendRichMessage("Valid commands are:");
			sender.sendRichMessage(usage);
		}
		else
			sender.sendRichMessage("There are no commands available to you");
		
		
	}
	
	public List<String> tabComplete( CommandSource sender, String parent, String label, String[] args )
	{
		parent += label + " ";
		
		List<String> results = new ArrayList<>();
		if(args.length == 1) // Tab completing the sub command
		{
			for(ICommand registeredCommand : mCommands.values())
			{
				if(registeredCommand.getName().toLowerCase().startsWith(args[0].toLowerCase()))
				{
					// Check that the sender is correct
					if(!registeredCommand.getAllowedSenders().contains(CommandSourceType.from(sender)))
						continue;
					
					// Check that they have permission
					if(registeredCommand.getPermission() != null && !sender.hasPermission(registeredCommand.getPermission()))
						continue;
					
					results.add(registeredCommand.getName());
				}
			}
		}
		else if(args.length > 1)
		{
			// Find the command to use
			String subCommand = args[0].toLowerCase();
			String[] subArgs = (args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
			
			ICommand com = null;
			if(mCommands.containsKey(subCommand))
			{
				com = mCommands.get(subCommand);
			}
			else
			{
				// Check aliases
	AliasCheck:	for(Entry<String, ICommand> ent : mCommands.entrySet())
				{
					if(ent.getValue().getAliases() != null)
					{
						String[] aliases = ent.getValue().getAliases();
						for(String alias : aliases)
						{
							if(subCommand.equalsIgnoreCase(alias))
							{
								com = ent.getValue();
								break AliasCheck;
							}
						}
					}
				}
			}
			
			// Was not found
			if(com == null)
			{
				return results;
			}
			
			// Check that the sender is correct
			if(!com.getAllowedSenders().contains(CommandSourceType.from(sender)))
				return results;
			
			// Check that they have permission
			if(com.getPermission() != null && !sender.hasPermission(com.getPermission()))
				return results;
			
			results = com.onTabComplete(sender, parent, subCommand, subArgs);
			if(results == null)
				return new ArrayList<>();
		}
		return results;
	}
	
	public static String colorUsage(String usage)
	{
		Matcher matcher = usageArgumentPattern.matcher(usage);
		StringBuffer buffer = new StringBuffer();
		
		while(matcher.find())
		{
			String str;
			if(matcher.group(1) != null)
				str = "<green>" + matcher.group(1);
			else if(matcher.group(2) != null)
				str = "<green>" + matcher.group(2);
			else
				str = "<gold>" + matcher.group(3);
			
			matcher.appendReplacement(buffer, str);
		}
		
		matcher.appendTail(buffer);
		
		return buffer.toString();
	}
	
	private class InternalHelp implements ICommand
	{
		@Override
		public String getName()
		{
			return "help";
		}

		@Override
		public String[] getAliases()
		{
			return null;
		}

		@Override
		public String getPermission()
		{
			return null;
		}

		@Override
		public String getUsageString( String label, CommandSource sender )
		{
			return label;
		}

		@Override
		public String getDescription()
		{
			return "Displays this screen.";
		}

		@Override
		public EnumSet<CommandSourceType> getAllowedSenders()
		{
			return EnumSet.allOf(CommandSourceType.class);
		}
		
		@Override
		public boolean onCommand( CommandSource sender, String parent, String label, String[] args )
		{
			if(args.length != 0)
				return false;
			
			sender.sendRichMessage("");
			sender.sendRichMessage("<yellow>" + parent + "<gold><command>");
			sender.sendRichMessage("<gray>\u25B7 " + mRootCommandDescription);
			sender.sendRichMessage("<yellow>Available commands:");
			
			if(mDefaultCommand != null)
			{
				if(mDefaultCommand.getAllowedSenders().contains(CommandSourceType.from(sender)) && (mDefaultCommand.getPermission() == null || sender.hasPermission(mDefaultCommand.getPermission())))
				{
					sender.sendRichMessage("<white>" + parent + "<yellow>" + colorUsage(mDefaultCommand.getUsageString(mDefaultCommand.getName(), sender)));
					
					String[] descriptionLines = mDefaultCommand.getDescription().split("\n");
					for(String line : descriptionLines)
						sender.sendRichMessage("<gray> \u25B7 " + line);
				}
			}
			
			for(ICommand command : mCommands.values())
			{
				// Dont show commands that are irrelevant
				if(!command.getAllowedSenders().contains(CommandSourceType.from(sender)))
					continue;
				
				if(command.getPermission() != null && !sender.hasPermission(command.getPermission()))
					continue;
				
				
				sender.sendRichMessage(" <white>" + parent + "<yellow>" + colorUsage(command.getUsageString(command.getName(), sender)));
				
				String[] descriptionLines = command.getDescription().split("\n");
				for(String line : descriptionLines)
					sender.sendRichMessage("<gray> \u25B7 " + line);
			}
			return true;
		}

		@Override
		public List<String> onTabComplete( CommandSource sender, String parent, String label, String[] args )
		{
			return null;
		}
		
	}
}
