package au.com.addstar.rcon.network;

import java.net.ConnectException;

import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientConnection
{
	private int mPort;
	private String mHost;
	
	private Channel mChannel;
	private EventLoopGroup mWorker;
	
	public ClientConnection(String host, int port)
	{
		mHost = host;
		mPort = port;
	}
	
	public void run(HandlerCreator handlerCreator) throws ConnectException
	{
		mWorker = new NioEventLoopGroup();
		
		Bootstrap builder = new Bootstrap();
		builder.group(mWorker)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new NetworkInitializer(handlerCreator));
		
		mChannel = builder.connect(mHost, mPort).syncUninterruptibly().channel();
	}
	
	public void sendPacket(RconPacket packet)
	{
		mChannel.writeAndFlush(packet).syncUninterruptibly();
	}
	
	public void shutdown()
	{
		mWorker.shutdownGracefully().syncUninterruptibly();
	}
}
