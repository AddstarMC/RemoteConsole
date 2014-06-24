package au.com.addstar.rcon.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketPrepender extends MessageToByteEncoder<ByteBuf>
{
	@Override
	protected void encode( ChannelHandlerContext ctx, ByteBuf input, ByteBuf out ) throws Exception
	{
		int size = input.readableBytes();
		out.writeShort(size);
		out.writeBytes(input, input.readerIndex(), size);
	}
}
