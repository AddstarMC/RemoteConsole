package au.com.addstar.rcon.network;

import java.net.ConnectException;
import java.net.SocketException;
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
	private String mAlias;
	private String mId;
	private String mConsoleFormat;
	private boolean mReconnect;
	
	private Channel mChannel;
	private EventLoopGroup mWorker;
	
	private ArrayList<NetworkManager> mManagers;
	private MessageBuffer mBuffer;
	
	private boolean mLoginSuccess;
	private CountDownLatch mLoginLatch;
	
	public ClientConnection(String host, int port)
	{
		this(host, port, false, null);
	}
	
	public ClientConnection(String host, int port, boolean reconnect, String alias)
	{
		mHost = host;
		mPort = port;
		mReconnect = reconnect;
		mAlias = alias;
		
		mManagers = new ArrayList<>();
		mServerName = "Unknown Server";
		mBuffer = new MessageBuffer(ClientMain.maxConsoleLines);
		
		RconPacket.initialize();
	}
	
	public void connect(HandlerCreator handlerCreator) throws ConnectException, SocketException, InterruptedException
	{
		mManagers.clear();
		mWorker = new NioEventLoopGroup();
		
		Bootstrap builder = new Bootstrap();
		builder.group(mWorker)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new NetworkInitializer<>(handlerCreator, NetworkManager.class, mManagers));
		
		mChannel = builder.connect(mHost, mPort).sync().channel();
		
		getManager().waitForActive();
	}
	
	public void startLogin(String username, String password)
	{
		mLoginSuccess = true;
		((ClientLoginHandler)getManager().getNetHandler()).setLoginInfo(username, password);
		((ClientLoginHandler)getManager().getNetHandler()).setClientConnection(this);
		mLoginLatch = new CountDownLatch(1);
		sendPacket(new PacketInLoginBegin());
	}
	
	public boolean waitForLogin() throws InterruptedException
	{
		GenericFutureListener<Future<? super Void>> listener = new GenericFutureListener<Future<? super Void>>()
		{
			@Override
			public void operationComplete( Future<? super Void> future ) throws Exception
			{
				mLoginSuccess = false;
				mLoginLatch.countDown();
				shutdown();
			}
		};
		
		mChannel.closeFuture().addListener(listener);
		try
		{
			mLoginLatch.await();
			return mLoginSuccess;
		}
		finally
		{
			mChannel.closeFuture().removeListener(listener);
		}
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
		if (mAlias != null)
			return mAlias;
		else
			return mServerName;
	}
	
	public String getAlias()
	{
		return mAlias;
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
		if (mAlias != null)
			return String.format("%s - %s:%d", mAlias, mHost, mPort);
		else
			return String.format("%s:%d", mHost, mPort);
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
