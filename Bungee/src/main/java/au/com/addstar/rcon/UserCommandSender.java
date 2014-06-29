package au.com.addstar.rcon;

import java.util.Collection;
import java.util.Collections;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;

public class UserCommandSender implements CommandSender
{
	private BungeeUser mUser;
	
	public UserCommandSender(BungeeUser user)
	{
		mUser = user;
	}
	
	@Override
	public String getName()
	{
		return mUser.getName();
	}

	@Override
	@Deprecated
	public void sendMessage( String message )
	{
		mUser.getManager().sendPacket(new PacketOutMessage(RemoteConsolePlugin.formatMessage(message), MessageType.Directed));
	}
	
	public void sendMessage( String message, MessageType type )
	{
		mUser.getManager().sendPacket(new PacketOutMessage(message, type));
	}

	@Override
	@Deprecated
	public void sendMessages( String... messages )
	{
		for(String message : messages)
			sendMessage(message);
	}

	@Override
	public void sendMessage( BaseComponent... messages )
	{
		for(BaseComponent message : messages)
			sendMessage(message.toLegacyText());
	}

	@Override
	public void sendMessage( BaseComponent message )
	{
		sendMessage(message.toLegacyText());
	}

	@Override
	public Collection<String> getGroups()
	{
		return Collections.emptySet();
	}

	@Override
	public void addGroups( String... groups )
	{
		throw new UnsupportedOperationException("Console may not have groups");
	}

	@Override
	public void removeGroups( String... groups )
	{
		throw new UnsupportedOperationException("Console may not have groups");
	}

	@Override
	public boolean hasPermission( String permission )
	{
		return true;
	}

	@Override
	public void setPermission( String permission, boolean value )
	{
		throw new UnsupportedOperationException("Console has all permissions");
	}

	@Override
	public Collection<String> getPermissions()
	{
		return Collections.emptySet();
	}

}
