package au.com.addstar.rcon.network.packets.main;

import io.netty.buffer.ByteBuf;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerServer;
import au.com.addstar.rcon.network.packets.RconPacket;

public class PacketInPassword extends RconPacket
{
	public String previousPassword;
	public String newPassword;
	
	public PacketInPassword() {}
	
	public PacketInPassword(String previous, String newPassword)
	{
		this.previousPassword = previous;
		this.newPassword = newPassword;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		previousPassword = readString(packet);
		newPassword = readString(packet);
	}
	
	@Override
	public void write( ByteBuf packet )
	{
		writeString(previousPassword, packet);
		writeString(newPassword, packet);
	}
	
	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkMainHandlerServer)handler).handlePassword(this);
	}
}
