package au.com.addstar.rcon;

import au.com.addstar.rcon.network.ClientConnection;

public interface IConnectionListener
{
	/**
	 * Called when a connection is ready to be used
	 */
    void connectionJoin(ClientConnection connection);
	/**
	 * Called when a connection terminates
	 */
    void connectionEnd(ClientConnection connection);
}
