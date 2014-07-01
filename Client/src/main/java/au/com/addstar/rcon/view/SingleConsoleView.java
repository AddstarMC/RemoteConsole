package au.com.addstar.rcon.view;

import au.com.addstar.rcon.network.ClientConnection;

public class SingleConsoleView extends ConsoleView
{
	private ClientConnection mConnection;
	public SingleConsoleView(ClientConnection connection)
	{
		super(connection.getMessageBuffer());
		mConnection = connection;
	}
	
	@Override
	public boolean isHandling( ClientConnection connection )
	{
		return connection == mConnection;
	}

}
