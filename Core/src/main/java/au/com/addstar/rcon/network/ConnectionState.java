package au.com.addstar.rcon.network;

import java.util.HashMap;

import au.com.addstar.rcon.network.packets.RconPacket;

public enum ConnectionState
{
	Login,
	Main;
	
	private HashMap<Byte, Class<? extends RconPacket>> mRegistrations;
	private HashMap<Class<? extends RconPacket>, Byte> mReverseRegistrations;
	
	ConnectionState()
	{
		mRegistrations = new HashMap<>();
		mReverseRegistrations = new HashMap<>();
	}
	
	public void addPacketType(int id, Class<? extends RconPacket> packetClass)
	{
		mRegistrations.put((byte)id, packetClass);
		mReverseRegistrations.put(packetClass, (byte)id);
	}
	
	public Class<? extends RconPacket> getPacket(int id)
	{
		return mRegistrations.get((byte)id);
	}
	
	public Byte getPacketId(RconPacket packet)
	{
		return mReverseRegistrations.get(packet.getClass());
	}
}
