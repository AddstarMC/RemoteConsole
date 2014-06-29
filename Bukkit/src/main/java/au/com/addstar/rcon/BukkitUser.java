package au.com.addstar.rcon;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.server.User;
import au.com.addstar.rcon.server.auth.StoredPassword;

public class BukkitUser extends User
{
	private ConfigurationSection mSection;
	private UserCommandSender mSender;
	
	public BukkitUser(String name, ConfigurationSection section)
	{
		super(name);
		mSection = section;
	}
	
	@Override
	public StoredPassword getPassword()
	{
		String stored = mSection.getString("password", null);
		if(stored == null)
			return null;
		
		String[] parts = stored.split(":");
		if(parts.length != 2)
			return null;
		
		return new StoredPassword(parts[0], parts[1]);
	}
	
	public void setPassword(StoredPassword password)
	{
		mSection.set("password", String.format("%s:%s", password.getHash(), password.getSalt()));
	}
	
	public ConsoleCommandSender asCommandSender()
	{
		return mSender;
	}
	
	@Override
	public void setManager( NetworkManager manager )
	{
		super.setManager(manager);
		
		if(manager == null)
		{
			mSender.onRemove();
			mSender = null;
		}
		else
			mSender = new UserCommandSender(this);
	}

	public void sendMessage( String message, MessageType type )
	{
		mSender.sendMessage(message, type);
	}
}
