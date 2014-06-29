package au.com.addstar.rcon.commands.account;

import java.util.EnumSet;
import java.util.List;
import java.util.WeakHashMap;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import au.com.addstar.rcon.BungeeUser;
import au.com.addstar.rcon.RemoteConsolePlugin;
import au.com.addstar.rcon.commands.BadArgumentException;
import au.com.addstar.rcon.commands.CommandSenderType;
import au.com.addstar.rcon.commands.ICommand;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.auth.StoredPassword;

public class PasswordCommand implements ICommand
{
	private WeakHashMap<CommandSender, BungeeUser> mLastUser = new WeakHashMap<CommandSender, BungeeUser>();
	private WeakHashMap<CommandSender, String> mLastPasswords = new WeakHashMap<CommandSender, String>();
	
	@Override
	public String getName()
	{
		return "password";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.account.manage.password";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + ChatColor.GOLD + " <account> <oldpassword> <newpassword>";
	}

	@Override
	public String getDescription()
	{
		return "Changes or sets an accounts password";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}
	
	@SuppressWarnings( "deprecation" )
	protected final boolean handlePasswordChange(CommandSender sender, BungeeUser user, String newPassword, String oldPassword)
	{
		String lastPassword = mLastPasswords.remove(sender);
		BungeeUser lastUser = mLastUser.remove(sender);
		
		if(lastPassword != null && user.equals(lastUser))
		{
			if(oldPassword == null)
			{
				if(lastPassword.equals(newPassword))
				{
					user.setPassword(StoredPassword.generate(newPassword));
					sender.sendMessage(user.getName() + "'s password has been changed");
					RemoteConsolePlugin.instance.getLogger().warning(user.getName() + "'s RCon password was changed by " + sender.getName());
					
					if(!RconServer.instance.saveUser(user))
						sender.sendMessage(ChatColor.RED + "Unable to save changes, an internal error occured.");
				}
				else
					throw new BadArgumentException(1, "The entered password does not match.");
				
				return true;
			}
		}

		if(user.getPassword() == null)
		{
			if(oldPassword != null)
				throw new IllegalArgumentException("No existing password exists, just use " + ChatColor.YELLOW + "/rcon account password " + user.getName() + " <password>");
			
			mLastPasswords.put(sender, newPassword);
			mLastUser.put(sender, user);
		}
		else
		{
			if(oldPassword == null)
				return false;

			if(!user.getPassword().matches(oldPassword))
			{
				sender.sendMessage(ChatColor.RED + "The specified password does not match the existing password!");
				RemoteConsolePlugin.instance.getLogger().warning(String.format("%s tried to change %s's RCon password", sender.getName(), user.getName()));
				return true;
			}
			
			mLastPasswords.put(sender, newPassword);
			mLastUser.put(sender, user);
		}
		
		sender.sendMessage("Please confirm the new password with /rcon account password " + user.getName() + " <newpassword>");
		return true;
	}
	
	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length != 2 && args.length != 3)
			return false;
		
		BungeeUser user = (BungeeUser)RconServer.instance.getUser(args[0]);
		
		if(user == null)
			throw new BadArgumentException(0, "Unknown account");
		
		String password = null;
		String oldPassword = null;
		
		if(args.length == 3)
		{
			password = args[2];
			oldPassword = args[1];
		}
		else
			password = args[1];
		
		return handlePasswordChange(sender, user, password, oldPassword);
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
