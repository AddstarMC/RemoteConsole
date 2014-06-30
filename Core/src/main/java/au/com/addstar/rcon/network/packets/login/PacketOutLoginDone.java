package au.com.addstar.rcon.network.packets.login;

import io.netty.buffer.ByteBuf;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerClient;
import au.com.addstar.rcon.network.packets.RconPacket;

public class PacketOutLoginDone extends RconPacket
{
	public String serverName;
	
	public PacketOutLoginDone() {}
	public PacketOutLoginDone(String serverName)
	{
		this.serverName = serverName;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		serverName = readString(packet);
	}

	@Override
	public void write( ByteBuf packet )
	{
		writeString(serverName, packet);
	}
	
	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkLoginHandlerClient)handler).handleLoginDone(this);
	}

}
