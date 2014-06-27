package au.com.addstar.rcon.network.packets.main;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerClient;
import au.com.addstar.rcon.network.packets.RconPacket;

public class PacketOutTabComplete extends RconPacket
{
	public List<String> results;
	
	public PacketOutTabComplete() {}
	public PacketOutTabComplete(List<String> results)
	{
		this.results = results;
	}
	
	@Override
	public void read( ByteBuf packet )
	{
		int size = packet.readShort();
		
		if(size == -1)
			results = null;
		else
		{
			results = new ArrayList<String>(size);
			for(int i = 0; i < size; ++i)
				results.add(readString(packet));
		}
	}

	@Override
	public void write( ByteBuf packet )
	{
		if(results == null)
			packet.writeShort(-1);
		else
		{
			packet.writeShort(results.size());
			for(String result : results)
				writeString(result, packet);
		}
	}
	
	@Override
	public void handlePacket( INetworkHandler handler )
	{
		((INetworkMainHandlerClient)handler).handleTabComplete(this);
	}

}
