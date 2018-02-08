package au.com.addstar.rcon.network;

import au.com.addstar.rcon.NetHandler;
import au.com.addstar.rcon.network.handlers.AbstractNetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerClient;
import au.com.addstar.rcon.network.packets.PacketOutDisconnect;
import au.com.addstar.rcon.network.packets.login.*;
import au.com.addstar.rcon.util.CryptHelper;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.crypto.SecretKey;
import java.util.Arrays;

public class ClientLoginHandler extends AbstractNetworkHandler implements INetworkLoginHandlerClient
{
	private enum State
	{
		Encrypt,
		Login,
		Accept
	}
	
	private State mState = State.Encrypt;
	
	private String mUsername;
	private String mPassword;
	
	private ClientConnection mConnection;
	
	public ClientLoginHandler(NetworkManager manager)
	{
		super(manager);
		this.debug = manager.isDebug();
	}
	
	public void setClientConnection(ClientConnection connection)
	{
		mConnection = connection;
	}
	
	public void setLoginInfo(String username, String password)
	{
		mUsername = username;
		mPassword = password;
	}
	
	@Override
	public void handleEncryptStart( PacketOutEncryptStart packet )
	{
		if(debug)System.out.println("Rcv PacketOutEncryptStart: packet:" + packet + " Key: " +packet.key + "Blob: "+ Arrays.toString(packet.randomBlob));

		if(mState != State.Encrypt)
		{
			getManager().close("Packet received out of sequence");
			return;
		}
		
		final SecretKey key = CryptHelper.generateSharedKey();
        if(debug)System.out.println("Generated Secretkey : " + key.toString());

        if(key == null){
			if(debug)System.out.println("Crpyto could not generate a shared Key..contact administration");
			//throw new NullPointerException("Crpyto could not generate a shared Key..contact administration");
            getManager().sendPacket(new PacketOutDisconnect());
		}else {
            mState = State.Login;
            if (debug)
                System.out.println("Sending PacketInEncryptGo: Priv:" + key.toString() + " Pub:" + packet.key.toString() + " Blob:" + packet.randomBlob);
            getManager().sendPacket(new PacketInEncryptGo(key, packet.key, packet.randomBlob)).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    getManager().enableEncryption(key);
                }
            });
        }
	}

	@Override
	public void handleLoginReady( PacketOutLoginReady packet )
	{
		if(debug)System.out.println("Recv PacketOutLoginReady: Packet:" + packet );

		if(mState != State.Login)
		{
			getManager().close("Packet received out of sequence");
			return;
		}
		
		getManager().sendPacket(new PacketInLogin(mUsername, mPassword));
		if(debug)System.out.println("Sending PacketInLogin: user:" + mUsername + "Pass: " +mPassword );
		mState = State.Accept;
	}
	
	@Override
	public void handleLoginDone( PacketOutLoginDone packet )
	{
		if(debug)System.out.println("Recv PacketOutLoginDne: Packet:" + packet );

		if(mState != State.Accept)
		{
			getManager().close("Packet received out of sequence");
			return;
		}
		
		getManager().transitionState(ConnectionState.Main);
		((NetHandler)getManager().getNetHandler()).setClientConnection(mConnection);
		if(debug)System.out.println("Finalizing Login: Client Connection is now " +getManager().getConnectionState().toString());

		mConnection.onLoginComplete(packet);
	}
}
