package au.com.addstar.rcon.view;

import au.com.addstar.rcon.MessageBuffer;
import au.com.addstar.rcon.network.ClientConnection;

public class NullConsoleView extends ConsoleView
{
	public NullConsoleView()
	{
		super(new MessageBuffer(1));
	}
	
	@Override
	public boolean isHandling( ClientConnection connection )
	{
		return false;
	}

}
