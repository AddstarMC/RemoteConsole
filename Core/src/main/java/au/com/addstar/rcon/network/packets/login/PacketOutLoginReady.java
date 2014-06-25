package au.com.addstar.rcon.network.packets.login;

import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerClient;
import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.buffer.ByteBuf;

public class PacketOutLoginReady extends RconPacket
{
	public int state;
	
	public PacketOutLoginReady() {}
	public PacketOutLoginReady(int state)
	{
		this.state = state;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		state = packet.readUnsignedByte();
	}

	@Override
	public void write( ByteBuf packet )
	{
		packet.writeByte(state);
	}
	
	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkLoginHandlerClient)handler).handleLoginReady(this);
	}

}
