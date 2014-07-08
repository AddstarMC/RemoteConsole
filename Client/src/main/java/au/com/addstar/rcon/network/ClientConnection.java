package au.com.addstar.rcon.network;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.MessageBuffer;
import au.com.addstar.rcon.network.packets.RconPacket;
import au.com.addstar.rcon.network.packets.login.PacketInLoginBegin;
import au.com.addstar.rcon.network.packets.login.PacketOutLoginDone;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ClientConnection
{
	private int mPort;
	private String mHost;
	private String mServerName;
	private String mId;
	private String mConsoleFormat;
	private boolean mReconnect;
	
	private Channel mChannel;
	private EventLoopGroup mWorker;
	
	private ArrayList<NetworkManager> mManagers;
	private MessageBuffer mBuffer;
	
	private CountDownLatch mLoginLatch;
	
	public ClientConnection(String host, int port)
	{
		this(host, port, false);
	}
	
	public ClientConnection(String host, int port, boolean reconnect)
	{
		mHost = host;
		mPort = port;
		mReconnect = reconnect;
		
		mManagers = new ArrayList<NetworkManager>();
		mServerName = "Unknown Server";
		mBuffer = new MessageBuffer(ClientMain.maxConsoleLines);
		
		RconPacket.initialize();
	}
	
	public void connect(HandlerCreator handlerCreator) throws ConnectException, InterruptedException
	{
		mManagers.clear();
		mWorker = new NioEventLoopGroup();
		
		Bootstrap builder = new Bootstrap();
		builder.group(mWorker)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new NetworkInitializer<NetworkManager>(handlerCreator, NetworkManager.class, mManagers));
		
		mChannel = builder.connect(mHost, mPort).sync().channel();
		
		getManager().waitForActive();
	}
	
	public void startLogin(String username, String password)
	{
		((ClientLoginHandler)getManager().getNetHandler()).setLoginInfo(username, password);
		((ClientLoginHandler)getManager().getNetHandler()).setClientConnection(this);
		mLoginLatch = new CountDownLatch(1);
		sendPacket(new PacketInLoginBegin());
	}
	
	public void waitForLogin() throws InterruptedException
	{
		mLoginLatch.await();
	}
	
	void onLoginComplete(PacketOutLoginDone packet)
	{
		mServerName = packet.serverName;
		mConsoleFormat = packet.consoleFormat;
		
		mLoginLatch.countDown();
	}
	
	public void sendPacket(RconPacket packet)
	{
		mChannel.writeAndFlush(packet).syncUninterruptibly();
	}
	
	public void shutdown()
	{
		mWorker.shutdownGracefully().syncUninterruptibly();
	}
	
	public NetworkManager getManager()
	{
		return mManagers.get(0);
	}
	
	public MessageBuffer getMessageBuffer()
	{
		return mBuffer;
	}

	public void addTerminationListener( GenericFutureListener<Future<? super Void>> listener )
	{
		mChannel.closeFuture().addListener(listener);
	}
	
	public void removeTerminationListener( GenericFutureListener<Future<? super Void>> listener )
	{
		mChannel.closeFuture().removeListener(listener);
	}
	
	public String getServerName()
	{
		return mServerName;
	}
	
	public boolean isLoggedIn()
	{
		return getManager().getConnectionState() == ConnectionState.Main;
	}
	
	public void setShouldReconnect(boolean value)
	{
		mReconnect = value;
	}
	public boolean shouldReconnect()
	{
		return mReconnect;
	}

	public void waitForShutdown() throws InterruptedException
	{
		mChannel.closeFuture().sync();
	}
	
	@Override
	public String toString()
	{
		return mHost + ":" + mPort;
	}
	
	public void setId(String id)
	{
		mId = id;
	}
	
	public String getId()
	{
		return mId;
	}
	
	public void setFormat(String format)
	{
		mConsoleFormat = format;
	}
	
	public String getFormat()
	{
		return mConsoleFormat;
	}
}
