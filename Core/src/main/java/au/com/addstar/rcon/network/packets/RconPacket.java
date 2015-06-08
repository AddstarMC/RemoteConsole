package au.com.addstar.rcon.network.packets;

import au.com.addstar.rcon.network.ConnectionState;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.packets.login.PacketInEncryptGo;
import au.com.addstar.rcon.network.packets.login.PacketInLogin;
import au.com.addstar.rcon.network.packets.login.PacketInLoginBegin;
import au.com.addstar.rcon.network.packets.login.PacketOutEncryptStart;
import au.com.addstar.rcon.network.packets.login.PacketOutLoginDone;
import au.com.addstar.rcon.network.packets.login.PacketOutLoginReady;
import au.com.addstar.rcon.network.packets.main.PacketInCommand;
import au.com.addstar.rcon.network.packets.main.PacketInPassword;
import au.com.addstar.rcon.network.packets.main.PacketInTabComplete;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutTabComplete;
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
	
	
	public static void initialize()
	{
		ConnectionState.Login.addPacketType(0, PacketInLoginBegin.class);
		ConnectionState.Login.addPacketType(1, PacketOutEncryptStart.class);
		ConnectionState.Login.addPacketType(2, PacketInEncryptGo.class);
		ConnectionState.Login.addPacketType(3, PacketOutLoginReady.class);
		ConnectionState.Login.addPacketType(4, PacketInLogin.class);
		ConnectionState.Login.addPacketType(5, PacketOutLoginDone.class);
		
		ConnectionState.Main.addPacketType(10, PacketInCommand.class);
		ConnectionState.Main.addPacketType(11, PacketOutMessage.class);
		ConnectionState.Main.addPacketType(12, PacketInTabComplete.class);
		ConnectionState.Main.addPacketType(13, PacketOutTabComplete.class);
		ConnectionState.Main.addPacketType(14, PacketInPassword.class);
		
		ConnectionState.Login.addPacketType(255, PacketOutDisconnect.class);
		ConnectionState.Main.addPacketType(255, PacketOutDisconnect.class);
	}
}
