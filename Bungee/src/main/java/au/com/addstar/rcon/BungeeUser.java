package au.com.addstar.rcon;

import net.md_5.bungee.api.CommandSender;
import au.com.addstar.rcon.config.UserConfig;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.server.User;
import au.com.addstar.rcon.server.auth.StoredPassword;

public class BungeeUser extends User
{
	private UserConfig mConfig;
	private UserCommandSender mSender;
	
	public BungeeUser(String name, UserConfig config)
	{
		super(name);
		mConfig = config;
	}
	
	@Override
	public StoredPassword getPassword()
	{
		if(mConfig.password == null)
			return null;
		
		String[] parts = mConfig.password.split(":");
		if(parts.length != 2)
			return null;
		
		return new StoredPassword(parts[0], parts[1]);
	}

	public void setPassword(StoredPassword password)
	{
		mConfig.password = String.format("%s:%s", password.getHash(), password.getSalt());
	}
	
	@Override
	public void setManager( NetworkManager manager )
	{
		super.setManager(manager);
		
		if(manager == null)
			mSender = null;
		else
			mSender = new UserCommandSender(this);
	}
	
	public CommandSender asCommandSender()
	{
		return mSender;
	}
}
