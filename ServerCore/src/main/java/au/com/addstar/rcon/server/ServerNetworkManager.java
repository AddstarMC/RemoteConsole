package au.com.addstar.rcon.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;

public class ServerNetworkManager extends NetworkManager implements GenericFutureListener<Future<? super Void>>
{
	private User mUser;
	public ServerNetworkManager(HandlerCreator creator)
	{
		super(creator);
	}
	
	public void setUser(User user)
	{
		if(mUser != null)
			throw new IllegalStateException("User already set. Cannot reset user");
		
		mUser = user;
	}
	
	public User getUser()
	{
		return mUser;
	}
	
	@Override
	public void channelActive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelActive(ctx);
		
		ctx.channel().closeFuture().addListener(this);
	}

	@Override
	public void operationComplete( Future<? super Void> future ) throws Exception
	{
		if(mUser != null)
			mUser.setManager(null);
		RconServer.instance.connectionClose(this, getDisconnectReason());
	}
}
