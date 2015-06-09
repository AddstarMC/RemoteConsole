package au.com.addstar.rcon;

import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.auth.IUserStore;
import au.com.addstar.rcon.server.auth.StoredPassword;

public class BukkitRconServer extends RconServer
{
	public BukkitRconServer(int port, String name, IUserStore userstore)
	{
		super(port, name, userstore);
	}
	
	protected BukkitUser createUser( String name )
	{
		return new BukkitUser(name);
	}
	
	@Override
	public boolean createUser( String name, StoredPassword password, boolean restricted )
	{
		BukkitUser user = createUser(name);
		user.setPassword(password);
		user.setIsRestricted(restricted);
		return addUser(user);
	}
	
	@Override
	public String getConsoleFormat()
	{
		return RemoteConsolePlugin.instance.getConsoleFormat();
	}
}
