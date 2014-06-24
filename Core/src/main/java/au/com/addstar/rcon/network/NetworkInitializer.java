package au.com.addstar.rcon.network;

import java.util.List;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class NetworkInitializer extends ChannelInitializer<SocketChannel>
{
	private HandlerCreator mCreator;
	
	private List<NetworkManager> mManagers;
	
	public NetworkInitializer(HandlerCreator handlerCreator)
	{
		mCreator = handlerCreator;
	}
	
	public NetworkInitializer(HandlerCreator handlerCreator, List<NetworkManager> managerList)
	{
		mCreator = handlerCreator;
		mManagers = managerList;
	}
	
	@Override
	protected void initChannel( SocketChannel channel ) throws Exception
	{
		channel.pipeline().addLast("collector", new PacketCollector()).addLast("decoder", new PacketDecoder()).addLast("prepender", new PacketPrepender()).addLast("encoder", new PacketEncoder());

		NetworkManager manager = new NetworkManager();
		
		if(mManagers != null)
			mManagers.add(manager);
		
		channel.pipeline().addLast("handler", manager);
		manager.setNetHandler(mCreator.newHandler(manager));
	}
}
