package au.com.addstar.rcon.network.handlers;

import au.com.addstar.rcon.network.packets.login.PacketOutEncryptStart;
import au.com.addstar.rcon.network.packets.login.PacketOutLoginDone;
import au.com.addstar.rcon.network.packets.login.PacketOutLoginReady;

public interface INetworkLoginHandlerClient extends INetworkHandler
{
	void handleEncryptStart(PacketOutEncryptStart packet);
	
	void handleLoginReady(PacketOutLoginReady packet);
	
	void handleLoginDone(PacketOutLoginDone packet);
}
