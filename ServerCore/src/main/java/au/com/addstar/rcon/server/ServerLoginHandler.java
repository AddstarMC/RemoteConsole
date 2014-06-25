package au.com.addstar.rcon.server;

import java.util.Arrays;
import java.util.Random;

import javax.crypto.SecretKey;

import au.com.addstar.rcon.network.ConnectionState;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.AbstractNetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerServer;
import au.com.addstar.rcon.network.packets.login.PacketInEncryptGo;
import au.com.addstar.rcon.network.packets.login.PacketInLogin;
import au.com.addstar.rcon.network.packets.login.PacketInLoginBegin;
import au.com.addstar.rcon.network.packets.login.PacketOutEncryptStart;
import au.com.addstar.rcon.network.packets.login.PacketOutLoginReady;
import au.com.addstar.rcon.util.CryptHelper;

public class ServerLoginHandler extends AbstractNetworkHandler implements INetworkLoginHandlerServer
{
	private enum State
	{
		Begin,
		Encrypt,
		Login
	}
	
	private State mCurrentState = State.Begin;
	private static Random mRand = new Random();
	
	private byte[] mBlob = new byte[14];
	
	public ServerLoginHandler(NetworkManager manager)
	{
		super(manager);
	}
	
	@Override
	public void handleLoginBegin(PacketInLoginBegin packet)
	{
		if(mCurrentState != State.Begin) 
		{
			disconnect("Packet sent out of sequence");
			return;
		}
		
		mRand.nextBytes(mBlob);
		getManager().sendPacket(new PacketOutEncryptStart(RconServer.getServerKey().getPublic(), mBlob));
		mCurrentState = State.Encrypt;
	}
	
	@Override
	public void handleEncryptGo(PacketInEncryptGo packet)
	{
		if(mCurrentState != State.Encrypt)
		{
			disconnect("Packet sent out of sequence");
			return;
		}
		
		byte[] blob = CryptHelper.decrypt(RconServer.getServerKey().getPrivate(), packet.randomBlob);
		
		if(!Arrays.equals(mBlob, blob))
		{
			disconnect("Key did not match");
			return;
		}
		
		SecretKey key = CryptHelper.decodeSecretKey(CryptHelper.decrypt(RconServer.getServerKey().getPrivate(), packet.secretKey));
		
		mCurrentState = State.Login;
		
		getManager().enableEncryption(key);
		
		getManager().sendPacket(new PacketOutLoginReady(1));
	}
	
	@Override
	public void handleLogin(PacketInLogin packet)
	{
		if(mCurrentState != State.Login)
		{
			disconnect("Packet sent out of sequence");
			return;
		}
		
		boolean auth = true;
		// TODO: Authentication
		
		if(!auth)
		{
			disconnect("Authentication failed");
			return;
		}
		else
		{
			System.out.println("[RCON] " + packet.username + " logged in on " + getManager().getAddress());
			getManager().sendPacket(new PacketOutLoginReady(2));
		}
		
		getManager().transitionState(ConnectionState.Main);
	}
}
