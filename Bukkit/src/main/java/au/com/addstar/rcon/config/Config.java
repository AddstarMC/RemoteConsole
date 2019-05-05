package au.com.addstar.rcon.config;

import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;

public class Config extends AutoConfig
{
	public Config(File file)
	{
		super(file);
	}
	
	@ConfigField
	public int port = 22050;
	
	@ConfigField(comment="The storage mode (where the account data will be stored)\nValid values are:\nfile, mysql")
	public String store = "file";
	
	@ConfigField(comment="The server name to display to the client. If left blank, the name configured in server.properties will be used.")
	public String serverName = "";

	@ConfigField(comment = "Set True for debugging")
	public boolean debug = false;

	@ConfigField(name="host", category="database")
	public String databaseHost = "localhost:3306";
	@ConfigField(name="database", category="database")
	public String databaseName = "RemoteConsole";
	@ConfigField(name="username", category="database")
	public String databaseUsername = "user";
	@ConfigField(name="password", category="database")
	public String databasePassword = "password";
	@ConfigField(name="useSSL", category="database")
	public String databaseUseSSL = "false";
	
	@Override
	protected void onPostLoad() throws InvalidConfigurationException
	{
		if(port < 0 || port > 65535)
			throw new InvalidConfigurationException("Port number must be between 0 and 65535");
		
		if(!store.equalsIgnoreCase("file") && !store.equalsIgnoreCase("mysql"))
			throw new InvalidConfigurationException("Store type must be 'file' or 'mysql'");
	}
	
}
