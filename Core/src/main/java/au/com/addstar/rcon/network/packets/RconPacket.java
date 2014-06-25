package au.com.addstar.rcon.network.packets;

import java.util.HashMap;

import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.packets.login.PacketInEncryptGo;
import au.com.addstar.rcon.network.packets.login.PacketInLogin;
import au.com.addstar.rcon.network.packets.login.PacketInLoginBegin;
import au.com.addstar.rcon.network.packets.login.PacketOutEncryptStart;
import au.com.addstar.rcon.network.packets.login.PacketOutLoginReady;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public abstract class RconPacket
{
	public abstract void read(ByteBuf packet);
	public abstract void write(ByteBuf packet);
	
	public void handlePacket(INetworkHandler handler)
	{
		throw new UnsupportedOperationException();
	}
	
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
	
	public static void writeBlob(byte[] blob, ByteBuf buffer)
	{
		buffer.writeByte(blob.length);
		buffer.writeBytes(blob);
	}
	
	public static byte[] readBlob(ByteBuf buffer)
	{
		int length = buffer.readUnsignedByte();
		byte[] data = new byte[length];
		buffer.readBytes(data);
		return data;
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

		addPacketType(0, PacketInLoginBegin.class);
		addPacketType(1, PacketOutEncryptStart.class);
		addPacketType(2, PacketInEncryptGo.class);
		addPacketType(3, PacketOutLoginReady.class);
		addPacketType(4, PacketInLogin.class);
		
		addPacketType(10, PacketInCommand.class);
		addPacketType(11, PacketOutMessage.class);
		
		addPacketType(255, PacketOutDisconnect.class);
	}
}
