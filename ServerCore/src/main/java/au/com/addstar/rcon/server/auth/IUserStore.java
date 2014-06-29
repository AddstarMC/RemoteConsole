package au.com.addstar.rcon.server.auth;

import java.io.IOException;

import au.com.addstar.rcon.server.User;

public interface IUserStore
{
	public void initialize() throws IOException;
	public void shutdown() throws IOException;
	
	public boolean loadUser(User user) throws IOException;
	public void saveUser(User user) throws IOException;
	
	public void addUser(User user) throws IOException;
	public void removeUser(User user) throws IOException;
}
