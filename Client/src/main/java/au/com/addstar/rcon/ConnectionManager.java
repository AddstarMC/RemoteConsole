package au.com.addstar.rcon;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.ConnectException;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import au.com.addstar.rcon.Event.EventType;
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
	
	private ArrayList<ClientConnection> mReconnectConnections;
	private ReconnectionThread mReconnectThread;
	
	private Object mConnectionLock = new Object();
	
	private HandlerCreator mHandlerCreator;
	
	private String mUsername;
	private String mPassword;
	
	public ConnectionManager(String username, String password)
	{
		mUsername = username;
		mPassword = password;
		mAllConnections = new ArrayList<ClientConnection>();
		mIdConnections = new HashMap<String, ClientConnection>();
		
		mPendingConnections = new ArrayDeque<ClientConnection>();
		mConnectingConnections = new HashSet<ClientConnection>();
		mReconnectConnections = new ArrayList<ClientConnection>();
		
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
	 * Adds a server to connect to upon {@link #connectAll}
	 * @param host The host name to connect to
	 * @param port The port number for the host
	 * @param reconnect When true, will reconnect upon connection lost
	 */
	public void addConnection(String host, int port, boolean reconnect)
	{
		mPendingConnections.add(new ClientConnection(host, port, reconnect));
	}
	
	/**
	 * Connects to all pending connections
	 * @throws InterruptedException
	 */
	public void connectAll() throws InterruptedException
	{
		while(!mPendingConnections.isEmpty())
		{
			ClientConnection connection = mPendingConnections.poll();
			connect(connection, false);
		}
	}
	
	/**
	 * Connects to the specified connection. NOTE: This does not check if the connection is already established
	 * @param connection The connection to connect 
	 * @param silent If true, no messages will be printed upon fail
	 * @return True if connection was formed.
	 * @throws InterruptedException
	 */
	private boolean connect(ClientConnection connection, boolean silent) throws InterruptedException
	{
		try
		{
			connection.connect(mHandlerCreator);
			mConnectingConnections.add(connection);
			connection.startLogin(mUsername, mPassword);
			ConnectionThread thread = new ConnectionThread(connection);
			thread.start();
			return true;
		}
		catch(UnresolvedAddressException e)
		{
			if(!silent)
				ClientMain.printErrMessage("Failed to connect to " + connection.toString());
			connection.shutdown();
		}
		catch(ConnectException e)
		{
			if(!silent)
				ClientMain.printErrMessage("Failed to connect to " + connection.toString());
			
			connection.shutdown();
			if(connection.shouldReconnect())
				scheduleReconnect(connection);
		}
		
		return false;
	}
	
	/**
	 * Schedules this connection to reconnect when possible
	 */
	private void scheduleReconnect(ClientConnection connection)
	{
		synchronized(mReconnectConnections)
		{
			if(!mReconnectConnections.contains(connection))
				mReconnectConnections.add(connection);
			
			if(mReconnectThread == null)
			{
				mReconnectThread = new ReconnectionThread();
				mReconnectThread.start();
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
			ClientMain.callEvent(new Event(EventType.ConnectionStart, connection));
			ClientMain.printErrMessage("Successfully logged into " + id);
			
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
			
			if(mActiveConnection == null)
			{
				mActiveConnection = connection;
				ClientMain.callEvent(new Event(EventType.ActiveServerChange, connection));
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
		
		ClientMain.printErrMessage(String.format("Disconnected from %s: %s", connection.getId(), message));
		
		synchronized(mIdConnections)
		{
			mIdConnections.remove(connection.getId());
		}
		ClientMain.callEvent(new Event(EventType.ConnectionShutdown, connection));
		
		synchronized(mConnectionLock)
		{
			mAllConnections.remove(connection);
		}
		
		if(connection.shouldReconnect())
			scheduleReconnect(connection);
	}
	
	/**
	 * Changes what connection is used as the primary one
	 * @param id The server id
	 */
	public void switchActive(String id)
	{
		if(id == null)
		{
			mActiveConnection = null;
			ClientMain.callEvent(new Event(EventType.ActiveServerChange, null));
			return;
		}
		
		synchronized(mIdConnections)
		{
			ClientConnection connection = mIdConnections.get(id);
			if(connection == null)
				throw new IllegalArgumentException("Unknown server " + id);
			
			mActiveConnection = connection;
			ClientMain.callEvent(new Event(EventType.ActiveServerChange, connection));
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
		if(mReconnectThread != null)
			mReconnectThread.interrupt();
		
		ArrayList<ClientConnection> connections = new ArrayList<ClientConnection>(mAllConnections);
		for(ClientConnection connection : connections)
		{
			connection.setShouldReconnect(false);
			connection.getManager().close(reason);
		}
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
				if(mAllConnections.isEmpty())
					break;
				
				ClientConnection connection = mAllConnections.get(0);
				connection.waitForShutdown();
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
					ClientMain.printErrMessage(String.format("Disconnected from %s: %s", mConnection, mConnection.getManager().getDisconnectReason()));
			}
		}
		
		@Override
		public void operationComplete( Future<? super Void> future ) throws Exception
		{
			interrupt();
			mConnection.shutdown();
		}
	}
	
	/**
	 * Attempts to reconnect to disconnected servers every 2 seconds
	 */
	private class ReconnectionThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					Thread.sleep(2000);
					
					synchronized(mReconnectConnections)
					{
						Iterator<ClientConnection> it = mReconnectConnections.iterator();
						while(it.hasNext())
						{
							ClientConnection connection = it.next();
							if(connect(connection, true))
								it.remove();
						}
					}
				}
			}
			catch(InterruptedException e)
			{
			}
		}
	}
}
