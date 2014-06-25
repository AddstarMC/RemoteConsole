package au.com.addstar.rcon.network.packets.login;

import java.security.PublicKey;

import javax.crypto.SecretKey;

import io.netty.buffer.ByteBuf;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerServer;
import au.com.addstar.rcon.network.packets.RconPacket;
import au.com.addstar.rcon.util.CryptHelper;

public class PacketInEncryptGo extends RconPacket
{
	public byte[] secretKey;
	public byte[] randomBlob;
	
	public PacketInEncryptGo() {}
	
	public PacketInEncryptGo(SecretKey secretKey, PublicKey key, byte[] randomBlob)
	{
		this.secretKey = CryptHelper.encrypt(key, secretKey.getEncoded());
		this.randomBlob = CryptHelper.encrypt(key, randomBlob);
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		randomBlob = readBlob(packet);
		secretKey = readBlob(packet);
	}

	@Override
	public void write( ByteBuf packet )
	{
		writeBlob(randomBlob, packet);
		writeBlob(secretKey, packet);
	}

	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkLoginHandlerServer)handler).handleEncryptGo(this);
	}
}
