package au.com.addstar.rcon.server;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;

import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkInitializer;
import au.com.addstar.rcon.network.packets.RconPacket;
import au.com.addstar.rcon.util.CryptHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public abstract class RconServer
{
	private int mPort;
	private EventLoopGroup mBoss;
	private EventLoopGroup mWorker;
	
	private KeyPair mServerKey;
	
	private ArrayList<ServerNetworkManager> mManagers;
	
	public static RconServer instance;
	
	public RconServer(int port)
	{
		instance = this;
		mPort = port;
		mManagers = new ArrayList<ServerNetworkManager>();
		
		mServerKey = CryptHelper.generateKey();
		RconPacket.initialize();
	}
	
	public void start(final HandlerCreator handlerCreator)
	{
		mBoss = new NioEventLoopGroup();
		mWorker = new NioEventLoopGroup();
		
		ServerBootstrap builder = new ServerBootstrap();
		builder.group(mBoss, mWorker)
			.channel(NioServerSocketChannel.class)
			.childHandler(new NetworkInitializer<ServerNetworkManager>(handlerCreator, ServerNetworkManager.class, mManagers))
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);
		
		builder.bind(mPort);
	}
	
	public void shutdown()
	{
		mBoss.shutdownGracefully().syncUninterruptibly();
		mWorker.shutdownGracefully().syncUninterruptibly();
	}
	
	public final KeyPair getServerKey()
	{
		return mServerKey;
	}
	
	public abstract User getUser(String name);
	
	public abstract void load() throws IOException;
	public abstract void save() throws IOException;
}
