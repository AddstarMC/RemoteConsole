package au.com.addstar.rcon.network;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.packets.RconPacket;
import au.com.addstar.rcon.util.CryptHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NetworkManager extends SimpleChannelInboundHandler<RconPacket>
{
	private INetworkHandler mHandler;
	private Channel mChannel;
	private SecretKey mSecretKey;
	
	private String mDCReason;
	
	public void setNetHandler(INetworkHandler handler)
	{
		mHandler = handler;
	}
	
	public void setSecretKey(SecretKey key)
	{
		mSecretKey = key;
	}
	
	public SecretKey getSecretKey()
	{
		return mSecretKey;
	}
	
	@Override
	protected void channelRead0( ChannelHandlerContext ctx, RconPacket msg ) throws Exception
	{
		msg.handlePacket(mHandler);
	}
	
	@Override
	public void channelActive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelActive(ctx);
		mChannel = ctx.channel();
	}
	
	@Override
	public void channelInactive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelInactive(ctx);
		close("Connection lost");
	}
	
	public void close(String reason)
	{
		if(mChannel.isOpen())
		{
			mChannel.close();
			mDCReason = reason;
		}
	}
	
	public String getDisconnectReason()
	{
		return mDCReason;
	}
	
	public ChannelFuture sendPacket(RconPacket packet)
	{
		return mChannel.writeAndFlush(packet);
	}
	
	public void enableEncryption()
	{
		mChannel.pipeline().addBefore("collector", "decrypter", new StreamDecrypter(CryptHelper.createContinuousCipher(Cipher.DECRYPT_MODE, mSecretKey)));
		mChannel.pipeline().addBefore("prepender", "encrypter", new StreamEncrypter(CryptHelper.createContinuousCipher(Cipher.ENCRYPT_MODE, mSecretKey)));
	}
}
