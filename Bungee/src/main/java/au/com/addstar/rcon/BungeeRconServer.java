package au.com.addstar.rcon;

import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.auth.IUserStore;
import au.com.addstar.rcon.server.auth.StoredPassword;

public class BungeeRconServer extends RconServer
{
	public BungeeRconServer(int port, IUserStore userstore)
	{
		super(port, userstore);
	}
	
	protected BungeeUser createUser(String name)
	{
		return new BungeeUser(name);
	}
	
	@Override
	public boolean createUser( String name, StoredPassword password )
	{
		BungeeUser user = createUser(name);
		user.setPassword(password);
		return addUser(user);
	}
}
