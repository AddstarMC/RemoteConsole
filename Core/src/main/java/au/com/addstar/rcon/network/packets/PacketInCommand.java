package au.com.addstar.rcon.network.packets;

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

}
