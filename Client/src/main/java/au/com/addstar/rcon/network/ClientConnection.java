package au.com.addstar.rcon.network;

import java.net.ConnectException;

import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
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
	
	public void run() throws ConnectException
	{
		mWorker = new NioEventLoopGroup();
		
		Bootstrap builder = new Bootstrap();
		builder.group(mWorker)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new ChannelInitializer<SocketChannel>()
			{
				@Override
				protected void initChannel( SocketChannel channel ) throws Exception
				{
					channel.pipeline().addLast("collector", new PacketCollector()).addLast("decoder", new PacketDecoder()).addLast("prepender", new PacketPrepender()).addLast("encoder", new PacketEncoder());
					channel.pipeline().addLast("handler", new Handler());
				}
				
				@Override
				public void channelInactive( ChannelHandlerContext ctx ) throws Exception
				{
				}
			});
		
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
	
	private class Handler extends SimpleChannelInboundHandler<RconPacket>
	{
		@Override
		protected void channelRead0( ChannelHandlerContext ctx, RconPacket msg ) throws Exception
		{
			
		}
		
		@Override
		public void channelInactive( ChannelHandlerContext ctx ) throws Exception
		{
			if(ctx.channel().isOpen())
				ctx.channel().close();
			
			System.out.println("Connection lost to server");
		}
		
		@Override
		public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception
		{
			System.out.println("Internal Exception: " + cause);
		}
	}
}
