package au.com.addstar.rcon.network;

import java.util.List;

import javax.crypto.Cipher;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class StreamDecrypter extends ByteToMessageDecoder
{
	private final Cipher mCipher;
	
	private byte[] buffer = new byte[0];
	
	public StreamDecrypter(Cipher cipher)
	{
		mCipher = cipher;
	}
	
	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception
	{
		int count = in.readableBytes();
		if(buffer.length < count)
			buffer = new byte[count];
		
		in.readBytes(buffer, 0, count);
		
		ByteBuf result = in.alloc().heapBuffer(mCipher.getOutputSize(count));
		
		result.writerIndex(mCipher.update(buffer, 0, count, result.array(), result.arrayOffset()));
		
		out.add(result);
	}
}
