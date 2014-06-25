package au.com.addstar.rcon.network.packets.login;

import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerClient;
import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.buffer.ByteBuf;

public class PacketOutLoginReady extends RconPacket
{
	@Override
	public void read( ByteBuf packet )
	{
	}

	@Override
	public void write( ByteBuf packet )
	{
	}
	
	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkLoginHandlerClient)handler).handleLoginReady(this);
	}

}
