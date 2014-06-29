package au.com.addstar.rcon;

import net.md_5.bungee.api.CommandSender;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.server.User;

public class BungeeUser extends User
{
	private UserCommandSender mSender;
	
	public BungeeUser(String name)
	{
		super(name);
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
