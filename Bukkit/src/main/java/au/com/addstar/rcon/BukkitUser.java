package au.com.addstar.rcon;

import org.bukkit.command.ConsoleCommandSender;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.server.User;

public class BukkitUser extends User
{
	private UserCommandSender mSender;
	
	public BukkitUser(String name)
	{
		super(name);
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
