package au.com.addstar.rcon.network.packets;

import io.netty.buffer.ByteBuf;

public class PacketOutDisconnect extends RconPacket
{
	public String reason;
	
	public PacketOutDisconnect() {}
	public PacketOutDisconnect(String reason)
	{
		this.reason = reason;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		reason = readString(packet);
	}

	@Override
	public void write( ByteBuf packet )
	{
		writeString(reason, packet);
	}

}
