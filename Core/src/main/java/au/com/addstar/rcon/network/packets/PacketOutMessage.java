package au.com.addstar.rcon.network.packets;

import io.netty.buffer.ByteBuf;

public class PacketOutMessage extends RconPacket
{
	public String message;
	
	public PacketOutMessage()
	{
	}
	
	public PacketOutMessage(String message)
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
