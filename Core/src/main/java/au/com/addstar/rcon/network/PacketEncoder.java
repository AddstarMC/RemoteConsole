package au.com.addstar.rcon.network;

import java.io.IOException;
import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<RconPacket>
{
	@Override
	protected void encode( ChannelHandlerContext context, RconPacket packet, ByteBuf bytebuf ) throws Exception
	{
		NetworkManager manager = context.attr(NetworkManager.NETWORK_MANAGER).get();
		
		Byte id = manager.getConnectionState().getPacketId(packet);
		
		if(id == null)
			throw new IOException("Cannot serialize unregistered packet");
		
		bytebuf.writeByte(id);
		packet.write(bytebuf);
	}
	
}
