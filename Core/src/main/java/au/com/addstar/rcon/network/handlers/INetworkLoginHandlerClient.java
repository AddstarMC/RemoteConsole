package au.com.addstar.rcon.network.handlers;

import au.com.addstar.rcon.network.packets.login.PacketOutEncryptStart;
import au.com.addstar.rcon.network.packets.login.PacketOutLoginDone;
import au.com.addstar.rcon.network.packets.login.PacketOutLoginReady;

public interface INetworkLoginHandlerClient extends INetworkHandler
{
	public void handleEncryptStart(PacketOutEncryptStart packet);
	
	public void handleLoginReady(PacketOutLoginReady packet);
	
	public void handleLoginDone(PacketOutLoginDone packet);
}
