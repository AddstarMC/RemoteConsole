package au.com.addstar.rcon.network.handlers;

import au.com.addstar.rcon.network.packets.login.PacketInEncryptGo;
import au.com.addstar.rcon.network.packets.login.PacketInLogin;
import au.com.addstar.rcon.network.packets.login.PacketInLoginBegin;

public interface INetworkLoginHandlerServer extends INetworkHandler
{
	public void handleLoginBegin(PacketInLoginBegin packet);
	
	public void handleEncryptGo(PacketInEncryptGo packet);
	
	public void handleLogin(PacketInLogin packet);
}
