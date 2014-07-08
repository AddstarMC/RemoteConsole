package au.com.addstar.rcon;

import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.auth.IUserStore;
import au.com.addstar.rcon.server.auth.StoredPassword;

public class BungeeRconServer extends RconServer
{
	private String mConsoleFormat;
	
	public BungeeRconServer(int port, String name, IUserStore userstore)
	{
		super(port, name, userstore);
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
	
	@Override
	public String getConsoleFormat()
	{
		if(mConsoleFormat == null)
		{
			String date = System.getProperty("net.md_5.bungee.log-date-format", "HH:mm:ss");
			mConsoleFormat = String.format("%%d{%s} [%%level]: %%msg%%n", date);
		}
		
		return mConsoleFormat;
	}
}
