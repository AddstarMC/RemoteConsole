package au.com.addstar.rcon.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.addstar.rcon.IConnectionListener;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.util.Message;

public class ConfigConsoleView extends CombinedConsoleView implements IConnectionListener
{
	private ArrayList<String> mWhitelistServers = new ArrayList<>();
	private ArrayList<String> mBlacklistServers = new ArrayList<>();
	
	private MessageProcessor mProcessor = new MessageProcessor();
	
	public void includes(String server)
	{
		mWhitelistServers.add(server);
	}
	
	public void excludes(String server)
	{
		mBlacklistServers.add(server);
	}

	private String escapeSymbols(String string)
	{
		StringBuilder builder = new StringBuilder();
		for(char c : string.toCharArray())
		{
			if(c == '*')
				builder.append('.');
			else if(!Character.isAlphabetic(c) && !Character.isDigit(c))
				builder.append('\\');
			builder.append(c);
		}
		
		return builder.toString();
	}
	private boolean doesListContain(List<String> list, String server)
	{
		for(String value : list)
		{
			if(value.equals(server))
				return true;
			
			if(value.contains("*"))
			{
				Pattern pattern = Pattern.compile(escapeSymbols(value));
				Matcher matcher = pattern.matcher(server);
				if(matcher.matches())
					return true;
			}
		}
		return false;
	}
	
	private boolean isServerIncluded(String server)
	{
		// Empty whitelist means all servers are good
		if(mWhitelistServers.isEmpty())
			return !doesListContain(mBlacklistServers, server);
		else
			return doesListContain(mWhitelistServers, server) && !doesListContain(mBlacklistServers, server);
	}
	
	@Override
	public void connectionJoin( ClientConnection connection )
	{
		if(isServerIncluded(connection.getServerName()))
			addConnection(connection);
	}

	@Override
	public void connectionEnd( ClientConnection connection )
	{
		if(isServerIncluded(connection.getServerName()))
			removeConnection(connection);
	}
	
	public MessageProcessor getProcessor()
	{
		return mProcessor;
	}
	
	@Override
	public void addMessage( Message message )
	{
		message = mProcessor.process(message);
		if(message != null)
			super.addMessage(message);
	}
}
