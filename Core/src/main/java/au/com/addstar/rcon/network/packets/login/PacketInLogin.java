package au.com.addstar.rcon.network.packets.login;

import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkLoginHandlerServer;
import au.com.addstar.rcon.network.packets.RconPacket;
import io.netty.buffer.ByteBuf;

public class PacketInLogin extends RconPacket
{
	public String username;
	public String password;
	
	public PacketInLogin()
	{
	}
	
	public PacketInLogin(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		username = readString(packet);
		password = readString(packet);
	}

	@Override
	public void write( ByteBuf packet )
	{
		writeString(username, packet);
		writeString(password, packet);
	}

	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkLoginHandlerServer)handler).handleLogin(this);
	}
}
