package au.com.addstar.rcon.server.auth;

import java.io.IOException;

import au.com.addstar.rcon.server.User;

public interface IUserStore
{
	void initialize() throws IOException;
	void shutdown() throws IOException;
	
	boolean loadUser(User user) throws IOException;
	void saveUser(User user) throws IOException;
	
	void addUser(User user) throws IOException;
	void removeUser(User user) throws IOException;
}
