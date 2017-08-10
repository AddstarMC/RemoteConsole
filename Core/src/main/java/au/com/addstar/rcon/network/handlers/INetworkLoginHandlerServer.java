package au.com.addstar.rcon.network.handlers;

import au.com.addstar.rcon.network.packets.login.PacketInEncryptGo;
import au.com.addstar.rcon.network.packets.login.PacketInLogin;
import au.com.addstar.rcon.network.packets.login.PacketInLoginBegin;

public interface INetworkLoginHandlerServer extends INetworkHandler
{
	void handleLoginBegin(PacketInLoginBegin packet);
	
	void handleEncryptGo(PacketInEncryptGo packet);
	
	void handleLogin(PacketInLogin packet);
}
