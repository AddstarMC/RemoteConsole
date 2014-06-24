package au.com.addstar.rcon.server;

import au.com.addstar.rcon.network.PacketDecoder;
import au.com.addstar.rcon.network.PacketEncoder;
import au.com.addstar.rcon.network.PacketCollector;
import au.com.addstar.rcon.network.PacketPrepender;
import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RconServer
{
	private int mPort;
	private EventLoopGroup mBoss;
	private EventLoopGroup mWorker;
	
	public RconServer(int port)
	{
		mPort = port;
	}
	
	public void start()
	{
		mBoss = new NioEventLoopGroup();
		mWorker = new NioEventLoopGroup();
		
		ServerBootstrap builder = new ServerBootstrap();
		builder.group(mBoss, mWorker)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>()
			{
				@Override
				protected void initChannel( SocketChannel channel ) throws Exception
				{
					channel.pipeline().addLast("collector", new PacketCollector()).addLast("decoder", new PacketDecoder()).addLast("prepender", new PacketPrepender()).addLast("encoder", new PacketEncoder());
					channel.pipeline().addLast("handler", new PacketHandler());
				}
			})
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);
		
		builder.bind(mPort);
	}
	
	public void shutdown()
	{
		mBoss.shutdownGracefully().syncUninterruptibly();
		mWorker.shutdownGracefully().syncUninterruptibly();
	}
	
	private class PacketHandler extends SimpleChannelInboundHandler<RconPacket>
	{
		@Override
		protected void channelRead0( ChannelHandlerContext ctx, RconPacket msg ) throws Exception
		{
			System.out.println(ctx.channel().localAddress().toString() + ": " + msg);
		}
		
		@Override
		public void channelInactive( ChannelHandlerContext ctx ) throws Exception
		{
			if(ctx.channel().isOpen())
				ctx.channel().close();
			
			System.out.println("Connection reset: " + ctx.channel().localAddress());
		}
	}
}
