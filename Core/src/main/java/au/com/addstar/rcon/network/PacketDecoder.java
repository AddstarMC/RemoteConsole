package au.com.addstar.rcon.network;

import java.util.List;

import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class PacketDecoder extends ByteToMessageDecoder
{
	@Override
	protected void decode( ChannelHandlerContext context, ByteBuf bytebuf, List<Object> out ) throws Exception
	{
		NetworkManager manager = context.attr(NetworkManager.NETWORK_MANAGER).get();
		
		byte id = bytebuf.readByte();
		
		Class<? extends RconPacket> packetType = manager.getConnectionState().getPacket(id);
		if(packetType == null)
			throw new NullPointerException("Tried to load invalid packet id " + id);
		
		RconPacket packet = packetType.newInstance();
		packet.read(bytebuf);
		out.add(packet);
	}
	
	@Override
	protected void decodeLast( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception
	{
	}
}
