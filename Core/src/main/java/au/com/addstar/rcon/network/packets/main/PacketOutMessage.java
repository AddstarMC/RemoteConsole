package au.com.addstar.rcon.network.packets.main;

import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerClient;
import au.com.addstar.rcon.network.packets.RconPacket;
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
	
	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkMainHandlerClient)handler).handleMessage(this);
	}

}
