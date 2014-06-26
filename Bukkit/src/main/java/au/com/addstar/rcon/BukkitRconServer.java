package au.com.addstar.rcon;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.User;

public class BukkitRconServer extends RconServer
{
	private File mStorageFile;
	private HashMap<String, User> mUsers;
	
	private FileConfiguration mConfig;
	private Plugin mPlugin;
	
	public BukkitRconServer(int port, File storage, Plugin plugin)
	{
		super(port);
		mStorageFile = storage;
		mPlugin = plugin;
		
		mUsers = new HashMap<String, User>();
		mConfig = new YamlConfiguration();
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
			if(mStorageFile.exists())
				mConfig.load(mStorageFile);
			
			mUsers.clear();
			
			ConfigurationSection userSection = mConfig.getConfigurationSection("users");
			
			if(userSection != null)
			{
				for(String key : userSection.getKeys(false))
				{
					if(!userSection.isConfigurationSection(key))
						continue;
					
					User user = new BukkitUser(key, userSection.getConfigurationSection(key));
					
					if(user.getPassword() == null)
						mPlugin.getLogger().warning(String.format("User %s has no password! Account disabled until one is set.", key));
	
					mUsers.put(user.getName(), user);
				}
			}
			
			if(!mUsers.containsKey("Console"))
			{
				if(userSection == null)
					userSection = mConfig.createSection("users");
				
				User console = new BukkitUser("Console", userSection.createSection("Console"));
				mUsers.put("Console", console);
			}
			
			save();
		}
		catch(InvalidConfigurationException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public void save() throws IOException
	{
		mConfig.save(mStorageFile);
	}

	public BukkitUser createUser( String name )
	{
		BukkitUser user = new BukkitUser(name, mConfig.getConfigurationSection("users").createSection(name));
		mUsers.put(name, user);
		return user;
	}

	public void removeUser( BukkitUser user )
	{
		mConfig.getConfigurationSection("users").set(user.getName(), null);
		mUsers.remove(user.getName());
	}

}
