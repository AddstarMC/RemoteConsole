package au.com.addstar.rcon.server;

import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkInitializer;
import au.com.addstar.rcon.network.packets.RconPacket;
import au.com.addstar.rcon.server.auth.IUserStore;
import au.com.addstar.rcon.server.auth.StoredPassword;
import au.com.addstar.rcon.util.CryptHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class RconServer
{
	private int mPort;
	private String mName;
	private EventLoopGroup mBoss;
	private EventLoopGroup mWorker;
	
	private KeyPair mServerKey;
	
	private ArrayList<ServerNetworkManager> mManagers;
	
	private IUserStore mUserStore;
	private HashMap<String, User> mUsers;
	private boolean mCanConnect = false;
	
	private Whitelist mWhitelist;
	
	public static RconServer instance;
	
	public RconServer(int port, String name, IUserStore storage)
	{
		instance = this;
		mPort = port;
		mManagers = new ArrayList<>();
		mUserStore = storage;
		mUsers = new HashMap<>();
		mName = name;
		mWhitelist = new Whitelist();
		
		mServerKey = CryptHelper.generateKey();
		RconPacket.initialize();
	}
	
	public void start(final HandlerCreator handlerCreator) throws IOException
	{
		mUserStore.initialize();
		
		mBoss = new NioEventLoopGroup();
		mWorker = new NioEventLoopGroup();
		
		ServerBootstrap builder = new ServerBootstrap();
		builder.group(mBoss, mWorker)
			.channel(NioServerSocketChannel.class)
			.childHandler(new NetworkInitializer<>(handlerCreator, ServerNetworkManager.class, mManagers))
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);
		
		builder.bind(mPort);
	}
	
	public void shutdown() throws IOException
	{
		mBoss.shutdownGracefully().syncUninterruptibly();
		mWorker.shutdownGracefully().syncUninterruptibly();
		
		mUserStore.shutdown();
	}
	
	public final void openServer()
	{
		mCanConnect = true;
	}
	
	public final boolean canConnect()
	{
		return mCanConnect;
	}
	
	public final KeyPair getServerKey()
	{
		return mServerKey;
	}
	
	/**
	 * Should create a new user with the supplied name. It should *not* load it
	 */
	protected abstract User createUser(String name);
	
	public User getUser(String name)
	{
		return getUser(name, true);
	}
	
	public User getUser(String name, boolean silent)
	{
		if(mUsers.containsKey(name))
			return mUsers.get(name);
		
		User user = createUser(name);
		try
		{
			if(!mUserStore.loadUser(user))
				return null;
		}
		catch(IOException e)
		{
			System.err.println("[RCON] Unable to load account " + name + ":");
			e.printStackTrace();
			if (silent)
				return null;
			else
				throw new RuntimeException("Unable to load account");
		}
		
		mUsers.put(name, user);
		return user;
	}
	
	public boolean saveUser(User user)
	{
		try
		{
			mUserStore.saveUser(user);
			return true;
		}
		catch(IOException e)
		{
			System.err.println("[RCON] Unable to save account " + user.getName() + ":");
			e.printStackTrace();
			return false;
		}
	}
	
	public abstract boolean createUser(String name, StoredPassword password, boolean restircted);
	
	protected boolean addUser(User user)
	{
		try
		{
			mUserStore.addUser(user);
			mUsers.put(user.getName(), user);
			return true;
		}
		catch(IOException e)
		{
			System.err.println("[RCON] Unable to add account " + user.getName() + ":");
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean removeUser(User user)
	{
		try
		{
			mUserStore.removeUser(user);
			mUsers.remove(user.getName());
			return true;
		}
		catch(IOException e)
		{
			System.err.println("[RCON] Unable to remove account " + user.getName() + ":");
			e.printStackTrace();
			return false;
		}
	}
	
	public List<ServerNetworkManager> getConnections()
	{
		return mManagers;
	}
	
	public String getServerName()
	{
		return mName;
	}
	
	public abstract String getConsoleFormat();
	
	public Whitelist getWhitelist()
	{
		return mWhitelist;
	}

	void connectionClose( ServerNetworkManager manager, String reason ) {
		String username = "Unknown";
		String message = (reason == null) ? "Connection lost": reason;
		if (manager != null) {
			mManagers.remove(manager);
			if (manager.getUser() != null) {
				username = manager.getUser().getName();
				mUsers.remove(manager.getUser().getName());
			} else if (manager.getAddress() != null)
				username = manager.getAddress().toString();
		}
		System.out.println("[RCON] " + username + " Disconnected: " + message);
	}
}
