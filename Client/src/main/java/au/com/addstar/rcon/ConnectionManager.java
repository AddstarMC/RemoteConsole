package au.com.addstar.rcon;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.ConnectException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.ClientLoginHandler;
import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;

public class ConnectionManager
{
	private ClientConnection mActiveConnection;
	private ArrayList<ClientConnection> mAllConnections;
	private HashMap<String, ClientConnection> mIdConnections;

	private ArrayDeque<ClientConnection> mPendingConnections;
	private HashSet<ClientConnection> mConnectingConnections;
	private ArrayDeque<ClientConnection> mClosedConnections;
	
	private Object mConnectionLock = new Object();
	
	private HandlerCreator mHandlerCreator;
	
	public ConnectionManager()
	{
		mAllConnections = new ArrayList<ClientConnection>();
		mIdConnections = new HashMap<String, ClientConnection>();
		
		mPendingConnections = new ArrayDeque<ClientConnection>();
		mConnectingConnections = new HashSet<ClientConnection>();
		mClosedConnections = new ArrayDeque<ClientConnection>();
		
		mHandlerCreator = new HandlerCreator()
		{
			@Override
			public INetworkHandler newHandlerLogin( NetworkManager manager )
			{
				return new ClientLoginHandler(manager);
			}
			
			@Override
			public INetworkHandler newHandlerMain( NetworkManager manager )
			{
				return new NetHandler(manager);
			}
		};
	}
	
	/**
	 * Adds a server to connect to upon {@link #connectAll}
	 * @param host The host name to connect to
	 * @param port The port number for the host
	 */
	public void addConnection(String host, int port)
	{
		mPendingConnections.add(new ClientConnection(host, port));
	}
	
	/**
	 * Connects to all pending connections
	 * @param username The username to connect with
	 * @param password The password to connect with
	 * @throws InterruptedException
	 */
	public void connectAll(String username, String password) throws InterruptedException
	{
		while(!mPendingConnections.isEmpty())
		{
			ClientConnection connection = mPendingConnections.poll();
			try
			{
				connection.connect(mHandlerCreator);
				mConnectingConnections.add(connection);
				connection.startLogin(username, password);
				ConnectionThread thread = new ConnectionThread(connection);
				thread.start();
			}
			catch(ConnectException e)
			{
				System.err.println("Failed to connect to " + connection.toString());
				connection.shutdown();
			}
		}
	}
	
	/**
	 * Generates a unique ID for the server using the name of the server
	 */
	private String generateId(ClientConnection connection)
	{
		synchronized(mIdConnections)
		{
			if(!connection.isLoggedIn())
				return null;
			
			String baseName = connection.getServerName();
			baseName = baseName.replace(" ", "");
			
			String serverName = baseName;
			int id = 0;
			while(mIdConnections.containsKey(serverName))
			{
				++id;
				serverName = String.format("%s-%d", baseName, id);
			}
			
			return serverName;
		}
	}
	
	/**
	 * Called upon login succeeding
	 */
	private void onConnectionEstabolished(final ClientConnection connection)
	{
		synchronized(mIdConnections)
		{
			String id = generateId(connection);
			connection.setId(id);
			mIdConnections.put(id, connection);
			System.err.println("Successfully logged into " + id);
			
			connection.addTerminationListener(new GenericFutureListener<Future<? super Void>>()
			{
				@Override
				public void operationComplete( Future<? super Void> future ) throws Exception
				{
					onConnectionEnded(connection);
				}
			});
			
			mConnectingConnections.remove(connection);
			
			synchronized(mConnectingConnections)
			{
				mConnectingConnections.notifyAll();
			}
		}
		
		synchronized(mConnectionLock)
		{
			mAllConnections.add(connection);
		}
	}
	
	/**
	 * Called upon connection terminating
	 */
	private void onConnectionEnded(ClientConnection connection)
	{
		String message = connection.getManager().getDisconnectReason();
		if(message == null)
			message = "Connection lost";
		
		System.err.println(String.format("Disconnected from %s: %s", connection.getId(), message));
		
		synchronized(mIdConnections)
		{
			mIdConnections.remove(connection.getId());
		}
		synchronized(mConnectionLock)
		{
			mAllConnections.remove(connection);
			mClosedConnections.add(connection);
		}
		synchronized(mClosedConnections)
		{
			mClosedConnections.notifyAll();
		}
	}
	
	/**
	 * Changes what connection is used as the primary one
	 * @param id The server id
	 */
	public void switchActive(String id)
	{
		synchronized(mIdConnections)
		{
			ClientConnection connection = mIdConnections.get(id);
			if(connection == null)
				throw new IllegalArgumentException("Unknown server " + id);
			
			mActiveConnection = connection;
		}
	}
	
	public ClientConnection getActive()
	{
		return mActiveConnection;
	}
	
	/**
	 * Gets all servers currently connected to
	 * @return
	 */
	public Set<String> getConnectionNames()
	{
		synchronized(mIdConnections)
		{
			return new HashSet<String>(mIdConnections.keySet());
		}
	}
	
	public ClientConnection getConnection(String id)
	{
		synchronized(mIdConnections)
		{
			return mIdConnections.get(id);
		}
	}
	
	public void closeAll(String reason)
	{
		ArrayList<ClientConnection> connections = new ArrayList<ClientConnection>(mAllConnections);
		for(ClientConnection connection : connections)
			connection.getManager().close(reason);
	}
	
	public void waitUntilClosed() throws InterruptedException
	{
		// Wait for all connections to finish initializing
		while(!mConnectingConnections.isEmpty())
		{
			synchronized(mConnectingConnections)
			{
				mConnectingConnections.wait();
			}
		}
		
		// Wait for connections to close so they can be shutdown
		while(true)
		{
			synchronized(mConnectionLock)
			{
				if(mAllConnections.isEmpty() && mClosedConnections.isEmpty())
					break;
				
				while(!mClosedConnections.isEmpty())
				{
					ClientConnection connection = mClosedConnections.poll();
					connection.shutdown();
				}
				
				if(mAllConnections.isEmpty() && mClosedConnections.isEmpty())
					break;
			}
			
			synchronized(mClosedConnections)
			{
				mClosedConnections.wait();
			}
		}
	}
	
	private class ConnectionThread extends Thread implements GenericFutureListener<Future<? super Void>>
	{
		private ClientConnection mConnection;
		
		public ConnectionThread(ClientConnection connection)
		{
			super("LoginThread: " + connection.getManager().getAddress());
			mConnection = connection;
			mConnection.addTerminationListener(this);
		}

		@Override
		public void run()
		{
			try
			{
				mConnection.waitForLogin();
				mConnection.removeTerminationListener(this);
				onConnectionEstabolished(mConnection);
			}
			catch ( InterruptedException e )
			{
				if(mConnection.getManager().getDisconnectReason() != null)
					System.err.println(String.format("Disconnected from %s: %s", mConnection, mConnection.getManager().getDisconnectReason()));
			}
		}
		
		@Override
		public void operationComplete( Future<? super Void> future ) throws Exception
		{
			interrupt();
			mConnection.shutdown();
		}
		
		
	}
}
