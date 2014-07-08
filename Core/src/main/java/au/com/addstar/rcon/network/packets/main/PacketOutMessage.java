package au.com.addstar.rcon.network.packets.main;

import java.util.logging.Level;

import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerClient;
import au.com.addstar.rcon.network.packets.RconPacket;
import au.com.addstar.rcon.util.Message;
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
	
	public Message message;
	
	public PacketOutMessage()
	{
	}
	
	public PacketOutMessage(Message message)
	{
		this.message = message;
		if(message.getType() == MessageType.System)
			throw new IllegalArgumentException("Cannot send system message type");
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		long time = packet.readLong();
		Level level = Level.parse(String.valueOf(packet.readInt()));
		MessageType type = MessageType.values()[packet.readUnsignedByte()];
		String msg = readString(packet);
		String thread = readString(packet);
		String logger = readString(packet);
		
		message = new Message(msg, type, time, level, thread, logger);
	}

	@Override
	public void write( ByteBuf packet )
	{
		packet.writeLong(message.getTime());
		packet.writeInt(message.getLevel().intValue());
		packet.writeByte(message.getType().ordinal());
		writeString(message.getMessage(), packet);
		writeString(message.getThreadName(), packet);
		writeString(message.getLogger(), packet);
	}
	
	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkMainHandlerClient)handler).handleMessage(this);
	}

}
