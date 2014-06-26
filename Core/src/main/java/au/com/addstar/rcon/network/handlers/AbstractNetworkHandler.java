package au.com.addstar.rcon.network.handlers;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.packets.PacketOutDisconnect;


public abstract class AbstractNetworkHandler implements INetworkHandler
{
	private NetworkManager mManager;
	
	public AbstractNetworkHandler(NetworkManager manager)
	{
		mManager = manager;
	}
	
	public final NetworkManager getManager()
	{
		return mManager;
	}
	
	@Override
	public final void disconnect(final String reason)
	{
		getManager().sendPacket(new PacketOutDisconnect(reason)).addListener(new GenericFutureListener<Future<? super Void>>()
		{
			@Override
			public void operationComplete( Future<? super Void> future ) throws Exception
			{
				getManager().close(reason);
			}
		});
	}
}
