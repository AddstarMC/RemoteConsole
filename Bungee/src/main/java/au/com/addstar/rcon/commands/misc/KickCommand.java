package au.com.addstar.rcon.commands.misc;

import java.util.EnumSet;
import java.util.List;

import net.md_5.bungee.api.CommandSender;

import au.com.addstar.rcon.BungeeUser;
import au.com.addstar.rcon.commands.BadArgumentException;
import au.com.addstar.rcon.commands.CommandSenderType;
import au.com.addstar.rcon.commands.ICommand;
import au.com.addstar.rcon.server.RconServer;

public class KickCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "kick";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.kick";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <account> [<message>]";
	}

	@Override
	public String getDescription()
	{
		return "Disconnects a connection from the server";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.allOf(CommandSenderType.class);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length < 1)
			return false;
		
		BungeeUser user = (BungeeUser)RconServer.instance.getUser(args[0]);
		
		if(user == null)
			throw new BadArgumentException(0, "That account doesnt exist");
		
		if(user.getManager() == null)
			throw new BadArgumentException(0, "That account is not online");
		
		String reason = "Kicked from server";
		
		if(args.length >= 2)
		{
			StringBuilder builder = new StringBuilder();
			for(int i = 1; i < args.length; ++i)
			{
				if(i != 1)
					builder.append(" ");
				builder.append(args[i]);
			}
			
			reason = builder.toString();
		}
		
		user.getManager().getNetHandler().disconnect(reason);
		return true;
	}
	

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
