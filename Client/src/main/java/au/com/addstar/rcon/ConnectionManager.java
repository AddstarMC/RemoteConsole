package au.com.addstar.rcon;

import au.com.addstar.rcon.Event.EventType;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.ClientLoginHandler;
import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.channels.UnresolvedAddressException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager
{
	private ClientConnection mActiveConnection;
	private ArrayList<ClientConnection> mAllConnections;
	private HashMap<String, ClientConnection> mIdConnections;
	private HashMap<String, String> mAliases;

	private ArrayDeque<ClientConnection> mPendingConnections;
	private HashSet<ClientConnection> mConnectingConnections;
	
	private ArrayList<ClientConnection> mReconnectConnections;
	private ReconnectionThread mReconnectThread;
	
	private Object mConnectionLock = new Object();
	
	private HandlerCreator mHandlerCreator;
	
	private String mUsername;
	private String mPassword;
	private boolean debug;
	
	private ExecutorService mConnectionExecutor;

	public ConnectionManager(String username, String password, boolean debug) {
		mUsername = username;
		mPassword = password;
		mAllConnections = new ArrayList<>();
		mIdConnections = new HashMap<>();
		mAliases = new HashMap<>();
		this.debug = debug;
		mPendingConnections = new ArrayDeque<>();
		mConnectingConnections = new HashSet<>();
		mReconnectConnections = new ArrayList<>();

		mHandlerCreator = new HandlerCreator()
		{
			@Override
			public INetworkHandler newHandlerLogin( NetworkManager manager )
			{
				manager.setDebug(debug);
				return new ClientLoginHandler(manager);
			}

			@Override
			public INetworkHandler newHandlerMain( NetworkManager manager )
			{
				manager.setDebug(debug);
				return new NetHandler(manager);
			}
		};

		mConnectionExecutor = Executors.newCachedThreadPool();
	}

	@Deprecated
	public ConnectionManager(String username, String password)
	{
		this(username,password,false);
	}
	
	/**
	 * Adds a server to connect to upon {@link #connectAll}
	 * @param host The host name to connect to
	 * @param port The port number for the host
	 * @param name The name for this host
	 */
	public void addConnection(String host, int port, String name)
	{
		mPendingConnections.add(new ClientConnection(host, port, false, name));
	}
	
	/**
	 * Adds a server to connect to upon {@link #connectAll}
	 * @param host The host name to connect to
	 * @param port The port number for the host
	 * @param reconnect When true, will reconnect upon connection lost
	 * @param name If set, this name will be used as an alias
	 */
	public void addConnection(String host, int port, boolean reconnect, String name)
	{
		mPendingConnections.add(new ClientConnection(host, port, reconnect, name));
	}
	
	public void setAlias(String host, int port, String name)
	{
		mAliases.put(name, String.format("%s:%d", host, port));
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
	 * @throws InterruptedException
	 */
	private void connect(ClientConnection connection, boolean silent) throws InterruptedException
	{
		try
		{
			if (mConnectingConnections.add(connection)) {
				ConnectionThread thread = new ConnectionThread(connection, silent);
				mConnectionExecutor.execute(thread);
			}
		}
		catch(UnresolvedAddressException e)
		{
			if(!silent)
				ClientMain.getViewManager().addSystemMessage("Failed to connect to " + connection.toString());
			connection.shutdown();
		}
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
			ClientMain.getViewManager().addSystemMessage("Successfully logged into " + id);
			
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
				ClientMain.getConsole().setPromptText(connection.getId());
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
		
		ClientMain.getViewManager().addSystemMessage(String.format("Disconnected from %s: %s", connection.getId(), message));
		
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
			ClientMain.getConsole().setPromptText("");
			return;
		}
		
		synchronized(mIdConnections)
		{
			ClientConnection connection = mIdConnections.get(id);
			if(connection == null)
				throw new IllegalArgumentException("Unknown server " + id);
			
			ClientMain.getConsole().setPromptText(connection.getId());
			
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
			return new HashSet<>(mIdConnections.keySet());
		}
	}
	
	public ClientConnection getConnection(String id)
	{
		synchronized(mIdConnections)
		{
			return mIdConnections.get(id);
		}
	}
	
	public String resolveAlias(String name)
	{
		return mAliases.get(name);
	}
	
	public void closeAll(String reason)
	{
		if(mReconnectThread != null)
			mReconnectThread.interrupt();
		
		ArrayList<ClientConnection> connections = new ArrayList<>(mAllConnections);
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
	
	private class ConnectionThread implements Runnable
	{
		private ClientConnection mConnection;
		private boolean mSilent;
		
		public ConnectionThread(ClientConnection connection, boolean silent)
		{
			mConnection = connection;
			mSilent = silent;
		}

		@Override
		public void run()
		{
			try
			{
				mConnection.connect(mHandlerCreator);
				mConnection.startLogin(mUsername, mPassword);
				
				if (mConnection.waitForLogin())
					onConnectionEstabolished(mConnection);
				else
				{
					mConnectingConnections.remove(mConnection);
					if(mConnection.getManager().getDisconnectReason() != null)
					{
						if (mConnection.getManager().getDisconnectReason().equals("Server is starting up"))
							scheduleReconnect(mConnection);
						else {
							if(debug)System.out.println(String.format("Disconnected from %s: %s", mConnection, mConnection.getManager().getDisconnectReason()));
							ClientMain.getViewManager().addSystemMessage(String.format("Disconnected from %s: %s", mConnection, mConnection.getManager().getDisconnectReason()));
						}
					}
				}
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
			catch(ConnectException e)
			{
                e.printStackTrace();
                mConnectingConnections.remove(mConnection);
				if(!mSilent)
					ClientMain.getViewManager().addSystemMessage("Failed to connect to " + mConnection.toString());
				if(debug)System.out.println("Failed to connect to " + mConnection.toString());
				mConnection.shutdown();
				if(mConnection.shouldReconnect())
					scheduleReconnect(mConnection);
			}
			catch(UnresolvedAddressException e)
			{
			    e.printStackTrace();
				mConnectingConnections.remove(mConnection);
				if(!mSilent)
					ClientMain.getViewManager().addSystemMessage("Failed to connect to " + mConnection.toString() + " unknown host");
				if(debug)System.out.println("Failed to connect to " + mConnection.toString() + " unknown host");
				mConnection.shutdown();
			}
			catch(SocketException e)
			{
                e.printStackTrace();
                mConnectingConnections.remove(mConnection);
				if(!mSilent)
					ClientMain.getViewManager().addSystemMessage("Failed to connect to " + mConnection.toString() + " " + e.getMessage());
				if(debug)System.out.println("Failed to connect to " + mConnection.toString() + " " + e.getMessage());
				mConnection.shutdown();
			}
			catch (Exception e){
				e.printStackTrace();
			}
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
							it.remove();
							connect(connection, true);
						}
					}
				}
			}
			catch(InterruptedException e)
			{
                e.printStackTrace();
            }
		}
	}
}
