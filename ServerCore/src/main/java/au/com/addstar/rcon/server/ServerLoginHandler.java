package au.com.addstar.rcon.server;

import au.com.addstar.rcon.network.ConnectionState;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.AbstractNetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerServer;
import au.com.addstar.rcon.network.packets.login.*;
import au.com.addstar.rcon.util.CryptHelper;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Random;

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
		
		if(!RconServer.instance.canConnect())
		{
			disconnect("Server is starting up");
			return;
		}
		
		mRand.nextBytes(mBlob);
		if(debug)System.out.println("Sending PacketOutEncryptStart: Pub:" + RconServer.instance.getServerKey().getPublic().toString() +  " Blob:" + mBlob );
		getManager().sendPacket(new PacketOutEncryptStart(RconServer.instance.getServerKey().getPublic(), mBlob,getManager().isDebug()));
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
		CryptHelper.setDebug(getManager().isDebug());
		byte[] blob = CryptHelper.decrypt(RconServer.instance.getServerKey().getPrivate(), packet.randomBlob);
		if(debug)System.out.println("Checking Blob for match");

		if(!Arrays.equals(mBlob, blob))
		{
			disconnect("Key did not match");
			return;
		}
		if(debug)System.out.println("Blob Matched");
		if(debug)System.out.println("Decrypting Packet Key: "+ packet.secretKey.toString());
		SecretKey key = CryptHelper.decodeSecretKey(CryptHelper.decrypt(RconServer.instance.getServerKey().getPrivate(), packet.secretKey));
		
		mCurrentState = State.Login;
		
		getManager().enableEncryption(key);
		if(debug)System.out.println("Sending PacketOutLoginReady");
		getManager().sendPacket(new PacketOutLoginReady());
	}
	
	@Override
	public void handleLogin(PacketInLogin packet)
	{
		if(mCurrentState != State.Login)
		{
			disconnect("Packet sent out of sequence");
			return;
		}
		
		User user;
		try
		{
			user = RconServer.instance.getUser(packet.username, false);
			if(user == null)
			{
				disconnect("Authentication failed");
				return;
			}
		}
		catch(RuntimeException e)
		{
			disconnect("Internal error");
			return;
		}
		
		if(!user.getPassword().matches(packet.password))
		{
			disconnect("Authentication failed");
			return;
		}
		
		if(user.getManager() != null)
		{
			user.getManager().close("Logged in again");
			return;
		}
		
		user.setManager(getManager());
		((ServerNetworkManager)getManager()).setUser(user);
		
		System.out.println("[RCON] " + packet.username + " logged in on " + getManager().getAddress());
		getManager().sendPacket(new PacketOutLoginDone(RconServer.instance.getServerName(), RconServer.instance.getConsoleFormat()));
		
		getManager().transitionState(ConnectionState.Main);
	}
}
