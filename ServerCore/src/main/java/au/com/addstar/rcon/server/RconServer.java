package au.com.addstar.rcon.server;

import java.util.ArrayList;

import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkInitializer;
import au.com.addstar.rcon.network.NetworkManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RconServer
{
	private int mPort;
	private EventLoopGroup mBoss;
	private EventLoopGroup mWorker;
	
	private ArrayList<NetworkManager> mManagers;
	
	public RconServer(int port)
	{
		mPort = port;
		mManagers = new ArrayList<NetworkManager>();
	}
	
	public void start(final HandlerCreator handlerCreator)
	{
		mBoss = new NioEventLoopGroup();
		mWorker = new NioEventLoopGroup();
		
		ServerBootstrap builder = new ServerBootstrap();
		builder.group(mBoss, mWorker)
			.channel(NioServerSocketChannel.class)
			.childHandler(new NetworkInitializer(handlerCreator, mManagers))
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);
		
		builder.bind(mPort);
	}
	
	public void shutdown()
	{
		mBoss.shutdownGracefully().syncUninterruptibly();
		mWorker.shutdownGracefully().syncUninterruptibly();
	}
}
