package au.com.addstar.rcon.network.packets.main;

import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerClient;
import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.buffer.ByteBuf;

public class PacketOutMessage extends RconPacket
{
	public enum MessageType
	{
		Directed,
		Log,
		Exception,
		Chat,
		// Client side only
		System;
	}
	
	public String message;
	public MessageType type;
	
	public PacketOutMessage()
	{
	}
	
	public PacketOutMessage(String message, MessageType type)
	{
		this.message = message;
		if(type == MessageType.System)
			throw new IllegalArgumentException("Cannot send system message type");
		this.type = type;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		message = readString(packet);
		type = MessageType.values()[packet.readByte()];
	}

	@Override
	public void write( ByteBuf packet )
	{
		writeString(message, packet);
		packet.writeByte(type.ordinal());
	}
	
	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkMainHandlerClient)handler).handleMessage(this);
	}

}
