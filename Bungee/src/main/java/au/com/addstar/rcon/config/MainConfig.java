package au.com.addstar.rcon.config;

import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.Path;

public class MainConfig extends Config
{
	public int port = 22050;
	
	@Comments({"The storage mode (where the account data will be stored)","Valid values are:","file, mysql"})
	public String store = "file";
	
	@Path("database.host")
	public String databaseHost = "localhost:3306";
	@Path("database.database")
	public String databaseName = "RemoteConsole";
	@Path("database.username")
	public String databaseUsername = "user";
	@Path("database.password")
	public String databasePassword = "password";
	
	
	public void checkValid() throws InvalidConfigurationException
	{
		if(port < 0 || port > 65535)
			throw new InvalidConfigurationException("Port number must be between 0 and 65535");
		
		if(!store.equalsIgnoreCase("file") && !store.equalsIgnoreCase("mysql"))
			throw new InvalidConfigurationException("Store type must be 'file' or 'mysql'");
	}
	
}
