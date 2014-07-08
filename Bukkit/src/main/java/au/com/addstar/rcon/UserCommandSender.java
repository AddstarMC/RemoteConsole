package au.com.addstar.rcon;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ManuallyAbandonedConversationCanceller;
import org.bukkit.craftbukkit.v1_7_R3.conversations.ConversationTracker;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.util.Message;

public class UserCommandSender implements ConsoleCommandSender
{
	private final PermissibleBase mPerm = new PermissibleBase(this);
	private final ConversationTracker mConversations = new ConversationTracker();
	
	private BukkitUser mUser;
	
	public UserCommandSender(BukkitUser user)
	{
		mUser = user;
	}
	
	@Override
	public String getName()
	{
		return mUser.getName();
	}
	
	@Override
	public Server getServer()
	{
		return Bukkit.getServer();
	}

	@Override
	public void sendRawMessage( String message )
	{
		mUser.getManager().sendPacket(new PacketOutMessage(new Message(message, MessageType.Directed, RemoteConsolePlugin.instance.getLogger().getName())));
	}
	
	public void sendMessage( String message, MessageType type )
	{
		mUser.getManager().sendPacket(new PacketOutMessage(new Message(message, type, RemoteConsolePlugin.instance.getLogger().getName())));
	}
	
	@Override
	public void sendMessage( String message )
	{
		sendRawMessage(message);
	}

	@Override
	public void sendMessage( String[] messages )
	{
		for(String message : messages)
			sendMessage(message);
	}

	@Override
	public boolean isOp()
	{
		return true;
	}

	@Override
	public void setOp( boolean value )
	{
		throw new UnsupportedOperationException("Cannot change operator status of server console");
	}
	
	@Override
	public PermissionAttachment addAttachment( Plugin plugin )
	{
		return mPerm.addAttachment(plugin);
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, int ticks )
	{
		return mPerm.addAttachment(plugin, ticks);
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, String name, boolean value )
	{
		return mPerm.addAttachment(plugin, name, value);
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, String name, boolean value, int ticks )
	{
		return mPerm.addAttachment(plugin, name, value, ticks);
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions()
	{
		return mPerm.getEffectivePermissions();
	}

	@Override
	public boolean hasPermission( String permission )
	{
		return mPerm.hasPermission(permission);
	}

	@Override
	public boolean hasPermission( Permission permission )
	{
		return mPerm.hasPermission(permission);
	}

	@Override
	public boolean isPermissionSet( String perm )
	{
		return mPerm.isPermissionSet(perm);
	}

	@Override
	public boolean isPermissionSet( Permission perm )
	{
		return mPerm.isPermissionSet(perm);
	}

	@Override
	public void recalculatePermissions()
	{
		mPerm.recalculatePermissions();
	}
	
	public void onRemove()
	{
		mPerm.clearPermissions();
	}

	@Override
	public void removeAttachment( PermissionAttachment attachment )
	{
		mPerm.removeAttachment(attachment);
	}

	@Override
	public void abandonConversation( Conversation conversation )
	{
		mConversations.abandonConversation(conversation, new ConversationAbandonedEvent(conversation, new ManuallyAbandonedConversationCanceller()));
	}

	@Override
	public void abandonConversation( Conversation conversation, ConversationAbandonedEvent details )
	{
		mConversations.abandonConversation(conversation, details);
	}

	@Override
	public void acceptConversationInput( String input )
	{
		mConversations.acceptConversationInput(input);
	}

	@Override
	public boolean beginConversation( Conversation conversation )
	{
		return mConversations.beginConversation(conversation);
	}

	@Override
	public boolean isConversing()
	{
		return mConversations.isConversing();
	}
}
