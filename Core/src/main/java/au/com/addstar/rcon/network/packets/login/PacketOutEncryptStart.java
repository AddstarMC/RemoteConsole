package au.com.addstar.rcon.network.packets.login;

import java.security.PublicKey;

import io.netty.buffer.ByteBuf;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerClient;
import au.com.addstar.rcon.network.packets.RconPacket;
import au.com.addstar.rcon.util.CryptHelper;

public class PacketOutEncryptStart extends RconPacket
{
	public PublicKey key;
	public byte[] randomBlob;
	
	public PacketOutEncryptStart() {}
	
	public PacketOutEncryptStart(PublicKey key, byte[] randomBlob)
	{
		this.key = key;
		this.randomBlob = randomBlob;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		randomBlob = readBlob(packet);
		key = CryptHelper.decode(readBlob(packet));
	}

	@Override
	public void write( ByteBuf packet )
	{
		writeBlob(randomBlob, packet);
		writeBlob(key.getEncoded(), packet);
	}
	
	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkLoginHandlerClient)handler).handleEncryptStart(this);
	}

}
