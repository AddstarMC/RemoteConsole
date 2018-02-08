package au.com.addstar.rcon.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.lang.reflect.Constructor;
import java.util.List;

public class NetworkInitializer<T extends NetworkManager> extends ChannelInitializer<SocketChannel>
{
	private HandlerCreator mCreator;
	
	private List<T> mManagers;
	private Constructor<T> mManagerClass;
	
	public NetworkInitializer(HandlerCreator handlerCreator, Class<T> managerClass)
	{
		this(handlerCreator, managerClass, null);
	}
	
	public NetworkInitializer(HandlerCreator handlerCreator, Class<T> managerClass, List<T> managerList)
	{
		mCreator = handlerCreator;
		mManagers = managerList;
		
		try
		{
			mManagerClass = managerClass.getConstructor(HandlerCreator.class);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private T newManager()
	{
		try
		{
			return mManagerClass.newInstance(mCreator);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void initChannel( SocketChannel channel ) throws Exception
	{
		channel.pipeline().addLast("collector", new PacketCollector()).addLast("decoder", new PacketDecoder()).addLast("prepender", new PacketPrepender()).addLast("encoder", new PacketEncoder());
		
		T manager = newManager();
		
		if(mManagers != null)
			mManagers.add(manager);
		
		channel.pipeline().addLast("handler", manager);
		manager.setNetHandler(mCreator.newHandlerLogin(manager));
	}
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception
	{
		System.out.println("Exception caught " + cause);
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}
}
