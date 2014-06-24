package au.com.addstar.rcon.network;

import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NetworkManager extends SimpleChannelInboundHandler<RconPacket>
{
	private NetworkHandler mHandler;
	private Channel mChannel;
	
	private String mDCReason;
	
	public void setNetHandler(NetworkHandler handler)
	{
		mHandler = handler;
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
	
	public void sendPacket(RconPacket packet)
	{
		mChannel.writeAndFlush(packet);
	}
}
