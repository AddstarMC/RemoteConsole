package au.com.addstar.rcon.commands.account;

import java.util.EnumSet;
import java.util.List;
import java.util.WeakHashMap;

import au.com.addstar.rcon.VelocityUser;
import au.com.addstar.rcon.RemoteConsolePlugin;
import au.com.addstar.rcon.commands.BadArgumentException;
import au.com.addstar.rcon.commands.CommandSourceType;
import au.com.addstar.rcon.commands.ICommand;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.auth.StoredPassword;
import com.velocitypowered.api.command.CommandSource;

public class PasswordCommand implements ICommand
{
	private WeakHashMap<CommandSource, VelocityUser> mLastUser = new WeakHashMap<>();
	private WeakHashMap<CommandSource, String> mLastPasswords = new WeakHashMap<>();
	
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
	public String getUsageString( String label, CommandSource sender )
	{
		return label + "<gold> <account> <oldpassword> <newpassword>";
	}

	@Override
	public String getDescription()
	{
		return "Changes or sets an accounts password";
	}

	@Override
	public EnumSet<CommandSourceType> getAllowedSenders()
	{
		return EnumSet.of(CommandSourceType.Player, CommandSourceType.Console);
	}
	
	@SuppressWarnings( "deprecation" )
	protected final boolean handlePasswordChange(CommandSource sender, VelocityUser user, String newPassword, String oldPassword)
	{
		String lastPassword = mLastPasswords.remove(sender);
		VelocityUser lastUser = mLastUser.remove(sender);
		
		if(lastPassword != null && user.equals(lastUser))
		{
			if(oldPassword == null)
			{
				if(lastPassword.equals(newPassword))
				{
					user.setPassword(StoredPassword.generate(newPassword));
					sender.sendRichMessage(user.getName() + "'s password has been changed");
					RemoteConsolePlugin.logger.warning(user.getName() + "'s RCon password was changed by Admin");
					
					if(!RconServer.instance.saveUser(user))
						sender.sendRichMessage("<red>Unable to save changes, an internal error occured.");
				}
				else
					throw new BadArgumentException(1, "The entered password does not match.");
				
				return true;
			}
		}

		if(user.getPassword() == null)
		{
			if(oldPassword != null)
				throw new IllegalArgumentException("No existing password exists, just use <yellow>/rcon account password " + user.getName() + " <password>");
			
			mLastPasswords.put(sender, newPassword);
			mLastUser.put(sender, user);
		}
		else
		{
			if(oldPassword == null)
				return false;

			if(!user.getPassword().matches(oldPassword))
			{
				sender.sendRichMessage("<red>The specified password does not match the existing password!");
				RemoteConsolePlugin.logger.warning(String.format("%s tried to change %s's RCon password", "Admin", user.getName()));
				return true;
			}
			
			mLastPasswords.put(sender, newPassword);
			mLastUser.put(sender, user);
		}
		
		sender.sendRichMessage("Please confirm the new password with /rcon account password " + user.getName() + " <newpassword>");
		return true;
	}
	
	@Override
	public boolean onCommand( CommandSource sender, String parent, String label, String[] args )
	{
		if(args.length != 2 && args.length != 3)
			return false;
		
		VelocityUser user = (VelocityUser)RconServer.instance.getUser(args[0]);
		
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
	public List<String> onTabComplete( CommandSource sender, String parent, String label, String[] args )
	{
		return null;
	}

}
