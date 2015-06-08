package au.com.addstar.rcon.command;

import java.util.List;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.ConsoleScreen;
import au.com.addstar.rcon.network.packets.main.PacketInPassword;

public class PasswordCommand implements ICommand
{
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
	public String getUsage()
	{
		return "<command> <oldpassword> <newpassword>";
	}

	@Override
	public String getDescription()
	{
		return "Changes your password";
	}

	@Override
	public boolean onCommand( ConsoleScreen screen, String label, String[] args )
	{
		if(args.length != 2)
			return false;
		
		ClientMain.getConnectionManager().getActive().sendPacket(new PacketInPassword(args[0], args[1]));
		return true;
	}

	@Override
	public List<String> tabComplete( ConsoleScreen screen, String label, String[] args )
	{
		return null;
	}

}
