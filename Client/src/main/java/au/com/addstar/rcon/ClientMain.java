package au.com.addstar.rcon;

import java.net.ConnectException;

import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.packets.PacketInCommand;
import au.com.addstar.rcon.network.packets.login.PacketInLoginBegin;

public class ClientMain
{
	private static ClientConnection mConnection;
	
	public static void main(String[] args) throws Exception
	{
		mConnection = new ClientConnection("localhost", 22050);
		try
		{
			mConnection.run(new HandlerCreator()
			{
				@Override
				public INetworkHandler newHandlerLogin( NetworkManager manager )
				{
					return new ClientLoginHandler(manager);
				}
				
				@Override
				public INetworkHandler newHandlerMain( NetworkManager manager )
				{
					return null;
				}
			});
		}
		catch(ConnectException e)
		{
			System.out.println(e.getMessage());
			return;
		}
		
		mConnection.sendPacket(new PacketInLoginBegin());
		
		System.out.println("Sleeping");
		Thread.sleep(4000);
		System.out.println("Shutting down");
		mConnection.shutdown();
		System.out.println("Shut down");
	}
}
