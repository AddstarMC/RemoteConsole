package au.com.addstar.rcon;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import net.cubespace.Yamler.Config.InvalidConfigurationException;

import au.com.addstar.rcon.config.UserConfig;
import au.com.addstar.rcon.config.UserStorageConfig;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.User;

public class BungeeRconServer extends RconServer
{
	private File mStorageFile;
	private HashMap<String, User> mUsers;
	private UserStorageConfig mConfig;
	
	public BungeeRconServer(int port, File storage)
	{
		super(port);
		mStorageFile = storage;
		
		mUsers = new HashMap<String, User>();
		mConfig = new UserStorageConfig();
	}
	
	@Override
	public User getUser( String name )
	{
		return mUsers.get(name);
	}

	@Override
	public void load() throws IOException
	{
		try
		{
			mConfig.init(mStorageFile);
			mUsers.clear();
			
			for(Entry<String, UserConfig> user : mConfig.users.entrySet())
				mUsers.put(user.getKey(), new BungeeUser(user.getKey(), user.getValue()));
		}
		catch ( InvalidConfigurationException e )
		{
			throw new IOException(e);
		}
	}

	@Override
	public void save() throws IOException
	{
		try
		{
			mConfig.save();
		}
		catch ( InvalidConfigurationException e )
		{
			throw new IOException(e);
		}
	}

	public BungeeUser createUser(String name)
	{
		UserConfig config = new UserConfig();
		BungeeUser user = new BungeeUser(name, config);
		
		mUsers.put(name, user);
		mConfig.users.put(name, config);
		
		return user;
	}
	
	public void removeUser(BungeeUser user)
	{
		mConfig.users.remove(user.getName());
		mUsers.remove(user.getName());
	}
}
