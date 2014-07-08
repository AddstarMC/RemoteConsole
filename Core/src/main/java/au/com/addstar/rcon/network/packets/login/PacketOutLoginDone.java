package au.com.addstar.rcon.network.packets.login;

import io.netty.buffer.ByteBuf;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerClient;
import au.com.addstar.rcon.network.packets.RconPacket;

public class PacketOutLoginDone extends RconPacket
{
	public String serverName;
	public String consoleFormat;
	
	public PacketOutLoginDone() {}
	public PacketOutLoginDone(String serverName, String format)
	{
		this.serverName = serverName;
		this.consoleFormat = format;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		serverName = readString(packet);
		consoleFormat = readString(packet);
	}

	@Override
	public void write( ByteBuf packet )
	{
		writeString(serverName, packet);
		writeString(consoleFormat, packet);
	}
	
	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkLoginHandlerClient)handler).handleLoginDone(this);
	}

}
