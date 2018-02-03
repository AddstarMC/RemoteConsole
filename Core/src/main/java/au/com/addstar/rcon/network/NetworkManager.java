package au.com.addstar.rcon.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.packets.PacketOutDisconnect;
import au.com.addstar.rcon.network.packets.RconPacket;
import au.com.addstar.rcon.util.CryptHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public class NetworkManager extends SimpleChannelInboundHandler<RconPacket>
{
	public static final AttributeKey<NetworkManager> NETWORK_MANAGER = AttributeKey.valueOf("NETMANAGER");
	
	private INetworkHandler mHandler;
	private HandlerCreator mCreator;
	private Channel mChannel;

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug){
		this.debug = debug;
	}

	private boolean debug = false;
	
	private ConnectionState mState;
	private CountDownLatch mActiveWaiter = new CountDownLatch(1);
	
	private String mDCReason;
	
	public NetworkManager(HandlerCreator creator)
	{
		this(creator,false);
	}

	public NetworkManager(HandlerCreator creator, boolean debug)
	{
		mCreator = creator;
		mState = ConnectionState.Login;
		this.debug = debug;
	}
	
	public void setNetHandler(INetworkHandler handler)
	{
		mHandler = handler;
	}
	
	public INetworkHandler getNetHandler()
	{
		return mHandler;
	}
	
	public HandlerCreator getHandlerCreator()
	{
		return mCreator;
	}
	
	public ConnectionState getConnectionState()
	{
		return mState;
	}
	
	public void transitionState(ConnectionState newState)
	{
		switch(newState)
		{
		case Main:
			setNetHandler(mCreator.newHandlerMain(this));
			break;
		case Login:
			setNetHandler(mCreator.newHandlerLogin(this));
			break;
		}
		
		mState = newState;
	}
	
	
	@Override
	protected void channelRead0( ChannelHandlerContext ctx, RconPacket msg ) throws Exception
	{
		if(msg instanceof PacketOutDisconnect)
		{
			close(((PacketOutDisconnect)msg).reason);
			return;
		}
		msg.handlePacket(mHandler);
	}
	
	@Override
	public void channelActive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelActive(ctx);
		mChannel = ctx.channel();
		mChannel.attr(NETWORK_MANAGER).set(this);
		mActiveWaiter.countDown();
	}
	
	@Override
	public void channelInactive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelInactive(ctx);
		close("Disconnected");
	}
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception
	{
		if(cause instanceof IOException && cause.getMessage().contains("closed by the remote host")) // Why can't there be a specific exception?!
			close("Connection lost");
		else
		{
			cause.printStackTrace();
			close("Internal server error");
		}
	}
	
	public void close(String reason)
	{
		if(mChannel.isOpen())
		{
			mDCReason = reason;
			mChannel.close();
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
	
	public void enableEncryption(SecretKey key)
	{
		mChannel.pipeline().addBefore("collector", "decrypter", new StreamDecrypter(CryptHelper.createContinuousCipher(Cipher.DECRYPT_MODE, key)));
		mChannel.pipeline().addBefore("prepender", "encrypter", new StreamEncrypter(CryptHelper.createContinuousCipher(Cipher.ENCRYPT_MODE, key)));
	}
	
	public void waitForActive() throws InterruptedException
	{
		mActiveWaiter.await();
	}
	
	public SocketAddress getAddress()
	{
		return mChannel.remoteAddress();
	}
}
