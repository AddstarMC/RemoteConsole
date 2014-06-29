package au.com.addstar.rcon;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import au.com.addstar.rcon.server.User;
import au.com.addstar.rcon.server.auth.IUserStore;
import au.com.addstar.rcon.server.auth.StoredPassword;

public class YamlUserStore implements IUserStore
{
	private File mStorageFile;
	private FileConfiguration mConfig;
	private ConfigurationSection mUsers;
	
	public YamlUserStore(File file)
	{
		mStorageFile = file;
	}
	
	@Override
	public void initialize() throws IOException
	{
		try
		{
			mConfig = new YamlConfiguration();
			
			if(mStorageFile.exists())
				mConfig.load(mStorageFile);
			
			if(mConfig.isConfigurationSection("users"))
				mUsers = mConfig.getConfigurationSection("users");
			else
				mUsers = mConfig.createSection("users");
		}
		catch(InvalidConfigurationException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public void shutdown() throws IOException
	{
	}

	@Override
	public boolean loadUser( User user ) throws IOException
	{
		if(!mUsers.isConfigurationSection(user.getName()))
			return false;
		
		ConfigurationSection userData = mUsers.getConfigurationSection(user.getName());
		
		String stored = userData.getString("password", null);
		if(stored != null)
		{
			String[] parts = stored.split(":");
			if(parts.length == 2)
				user.setPassword(new StoredPassword(parts[0], parts[1]));
		}
		
		return true;
	}

	@Override
	public void saveUser( User user ) throws IOException
	{
		ConfigurationSection userData;
		
		if(!mUsers.isConfigurationSection(user.getName()))
			userData = mUsers.createSection(user.getName());
		else
			userData = mUsers.getConfigurationSection(user.getName());
		
		userData.set("password", String.format("%s:%s", user.getPassword().getHash(), user.getPassword().getSalt()));
		
		mConfig.save(mStorageFile);
	}

	@Override
	public void addUser( User user ) throws IOException
	{
		saveUser(user);
	}

	@Override
	public void removeUser( User user ) throws IOException
	{
		if(!mUsers.isConfigurationSection(user.getName()))
			return;
		
		mUsers.set(user.getName(), null);
		
		mConfig.save(mStorageFile);
	}

}
