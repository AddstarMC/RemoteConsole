package au.com.addstar.rcon.network.packets;

import au.com.addstar.rcon.network.NetworkHandler;
import io.netty.buffer.ByteBuf;

public class PacketInCommand extends RconPacket
{
	public String command;
	
	public PacketInCommand()
	{
	}
	
	public PacketInCommand(String command)
	{
		this.command = command;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		command = readString(packet);
	}

	@Override
	public void write( ByteBuf packet )
	{
		writeString(command, packet);
	}
	
	@Override
	public void handlePacket( NetworkHandler handler )
	{
		handler.handleCommand(this);
	}

}
