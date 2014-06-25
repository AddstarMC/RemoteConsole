package au.com.addstar.rcon.network.packets.main;

import io.netty.buffer.ByteBuf;
import au.com.addstar.rcon.network.packets.RconPacket;

public class PacketInTabComplete extends RconPacket
{
	public String message;
	
	public PacketInTabComplete() {}
	public PacketInTabComplete(String message)
	{
		this.message = message;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		message = readString(packet);
	}

	@Override
	public void write( ByteBuf packet )
	{
		writeString(message, packet);
	}

}
