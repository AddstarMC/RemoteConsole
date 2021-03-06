package au.com.addstar.rcon.command;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.view.ConsoleView;

public class FilterCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "filter";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getUsage()
	{
		return "<command> [options]";
	}

	@Override
	public String getDescription()
	{
		return "Sets the filter for the current view";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		ConsoleView view = ClientMain.getViewManager().getActive();
		
		if(args.length == 0)
		{
			screen.printString("Current filters:");
			StringBuilder builder = new StringBuilder();
			for(MessageType type : view.getFilter())
			{
				if(builder.length() != 0)
					builder.append(" ");
				builder.append(type.name().toLowerCase());
			}
			screen.printString(builder.toString());
		}
		else
		{
			EnumSet<MessageType> filters = EnumSet.noneOf(MessageType.class);

			for (String arg : args) {
				MessageType selected = null;
				for (MessageType type : MessageType.values()) {
					if (arg.equalsIgnoreCase(type.name())) {
						selected = type;
						break;
					}
				}
				if (selected == null)
					throw new IllegalArgumentException("Unknown filter " + arg);

				if (filters.contains(selected))
					throw new IllegalArgumentException("Filter " + arg + " has already been specified");

				filters.add(selected);
			}
			
			view.setFilter(filters);
			view.getBuffer().display(screen, filters);
			screen.printString("Filter set");
		}
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		ArrayList<String> results = new ArrayList<>();
		
		String part = args[args.length-1].toLowerCase();
		for(MessageType type : MessageType.values())
		{
			if(type.name().toLowerCase().startsWith(part))
				results.add(type.name().toLowerCase());
		}
		
		return results;
	}

}
