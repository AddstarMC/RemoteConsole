package au.com.addstar.rcon.network;

import javax.crypto.Cipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class StreamEncrypter extends MessageToByteEncoder<ByteBuf>
{
	private final Cipher mCipher;
	
	private byte[] buffer = new byte[0];
	private byte[] cryptBuffer = new byte[0];
	
	public StreamEncrypter(Cipher cipher)
	{
		mCipher = cipher;
	}
	
	@Override
	protected void encode( ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out ) throws Exception
	{
		int count = msg.readableBytes();
		
		if(buffer.length < count)
			buffer = new byte[count];
		
		msg.readBytes(buffer, 0, count);
		
		int outSize = mCipher.getOutputSize(count);
		
		if(cryptBuffer.length < outSize)
			cryptBuffer = new byte[outSize];
		
		out.writeBytes(cryptBuffer, 0, mCipher.update(buffer, 0, count, cryptBuffer));
	}
}
