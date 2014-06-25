package au.com.addstar.rcon.network.packets.login;

import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerServer;
import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.buffer.ByteBuf;

public class PacketInLoginBegin extends RconPacket
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
		((INetworkLoginHandlerServer)handler).handleLoginBegin(this);
	}
}
