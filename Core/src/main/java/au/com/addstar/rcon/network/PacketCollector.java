package au.com.addstar.rcon.network;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Makes sure the entire packet is available before sending down the pipeline
 */
public class PacketCollector extends ByteToMessageDecoder
{
	@Override
	protected void decode( ChannelHandlerContext context, ByteBuf buffer, List<Object> out ) throws Exception
	{
		if(buffer.readableBytes() < 2)
		{
			System.out.println("collecting- not enough");
			return;
		}
		
		buffer.markReaderIndex();
		
		int size = buffer.readUnsignedShort();
		
		if(!buffer.isReadable(size))
		{
			buffer.resetReaderIndex();
			System.out.println("collecting- packet not big enough. need " + size + " has " + buffer.readableBytes());
			return;
		}
		
		out.add(buffer.readBytes(size));
	}

}
