package au.com.addstar.rcon.network.packets;

import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public abstract class RconPacket
{
	public abstract void read(ByteBuf packet);
	public abstract void write(ByteBuf packet);
	
	public static void writeString(String string, ByteBuf buffer)
	{
		byte[] data = string.getBytes(CharsetUtil.UTF_8);
		buffer.writeShort(data.length);
		buffer.writeBytes(data);
	}
	
	public static String readString(ByteBuf buffer)
	{
		int length = buffer.readUnsignedShort();
		byte[] data = new byte[length];
		buffer.readBytes(data);
		return new String(data, CharsetUtil.UTF_8);
	}
	
	private static HashMap<Byte, Class<? extends RconPacket>> mRegistrations;
	private static HashMap<Class<? extends RconPacket>, Byte> mReverseRegistrations;
	
	public static void addPacketType(int id, Class<? extends RconPacket> packetClass)
	{
		mRegistrations.put((byte)id, packetClass);
		mReverseRegistrations.put(packetClass, (byte)id);
	}
	
	public static Class<? extends RconPacket> getPacket(int id)
	{
		return mRegistrations.get((byte)id);
	}
	
	public static Byte getPacketId(RconPacket packet)
	{
		return mReverseRegistrations.get(packet.getClass());
	}
	
	static
	{
		mRegistrations = new HashMap<Byte, Class<? extends RconPacket>>();
		mReverseRegistrations = new HashMap<Class<? extends RconPacket>, Byte>();
		
		addPacketType(10, PacketInCommand.class);
		addPacketType(11, PacketOutMessage.class);
	}
}
