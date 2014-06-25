package au.com.addstar.rcon;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.crypto.SecretKey;

import au.com.addstar.rcon.network.ConnectionState;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.AbstractNetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerClient;
import au.com.addstar.rcon.network.packets.login.PacketInEncryptGo;
import au.com.addstar.rcon.network.packets.login.PacketInLogin;
import au.com.addstar.rcon.network.packets.login.PacketOutEncryptStart;
import au.com.addstar.rcon.network.packets.login.PacketOutLoginReady;
import au.com.addstar.rcon.util.CryptHelper;

public class ClientLoginHandler extends AbstractNetworkHandler implements INetworkLoginHandlerClient
{
	private enum State
	{
		Encrypt,
		Login,
		Accept
	}
	
	private State mState = State.Encrypt;
	
	public ClientLoginHandler(NetworkManager manager)
	{
		super(manager);
	}
	
	@Override
	public void handleEncryptStart( PacketOutEncryptStart packet )
	{
		if(mState != State.Encrypt)
		{
			getManager().close("Packet received out of sequence");
			return;
		}
		
		final SecretKey key = CryptHelper.generateSharedKey();
		
		mState = State.Login;
		getManager().sendPacket(new PacketInEncryptGo(key, packet.key, packet.randomBlob)).addListener(new GenericFutureListener<Future<? super Void>>()
		{
			@Override
			public void operationComplete( Future<? super Void> future ) throws Exception
			{
				getManager().enableEncryption(key);
			}
		});
	}

	@Override
	public void handleLoginReady( PacketOutLoginReady packet )
	{
		if(packet.state == 1)
			handleLoginReady0();
		else if(packet.state == 2)
			handleLoginSuccess();
		else
			getManager().close("Packet received out of sequence");

		
	}
	
	private void handleLoginReady0()
	{
		if(mState != State.Login)
		{
			getManager().close("Packet received out of sequence");
			return;
		}
		
		getManager().sendPacket(new PacketInLogin("TestUser", "1234"));
	}
	
	private void handleLoginSuccess()
	{
		if(mState != State.Accept)
		{
			getManager().close("Packet received out of sequence");
			return;
		}
		
		getManager().transitionState(ConnectionState.Main);
	}

}
